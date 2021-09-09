package com.fa993.web;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiPredicate;

import com.fa993.core.exceptions.MangaFetchingException;
import com.fa993.core.exceptions.PageProcessingException;
import com.fa993.core.managers.AuthorManager;
import com.fa993.core.managers.GenreManager;
import com.fa993.core.managers.MangaManager;
import com.fa993.core.managers.PageManager;
import com.fa993.core.managers.ProblemChildManager;
import com.fa993.core.managers.SourceManager;
import com.fa993.core.managers.TitleManager;
import com.fa993.core.pojos.Chapter;
import com.fa993.core.pojos.Manga;
import com.fa993.retrieval.Scrapper;
import com.fa993.retrieval.SourceScrapper;
import com.fa993.retrieval.pojos.ChapterDTO;
import com.fa993.retrieval.pojos.MangaDTO;

import com.google.common.util.concurrent.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class MultiThreadScrapper {

    private final int runThreads;
    private final int watchThreads;

    private final List<SourceScrapper> sct;
    private boolean watching;

    private MangaManager mangaManager;
    private SourceManager sourceManager;
    private AuthorManager authorManager;
    private GenreManager genreManager;
    private PageManager pageManager;
    private TitleManager titleManager;

    private ProblemChildManager problemChildManager;

    private ExecutorService runExecutors;
    private ListeningExecutorService watchExecutors;
    private ExecutorService callbackExecutor;

    public MultiThreadScrapper(MangaManager mangaManager, SourceManager sourceManager, AuthorManager authorManager,
                               GenreManager genreManager, PageManager pageManager, TitleManager titleManager,
                               ProblemChildManager problemChildManager)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.mangaManager = mangaManager;
        this.sourceManager = sourceManager;
        this.authorManager = authorManager;
        this.genreManager = genreManager;
        this.pageManager = pageManager;
        this.titleManager = titleManager;
        this.problemChildManager = problemChildManager;
        this.runThreads = 32;
        this.watchThreads = 48;
        this.watchExecutors = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(0, watchThreads, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }));
        this.runExecutors = new ThreadPoolExecutor(0, runThreads, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        this.callbackExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        ScanResult rs = new ClassGraph().acceptPackages(this.getClass().getPackageName()).enableAllInfo().scan();
        this.sct = new ArrayList<>();
        rs.getClassesWithAnnotation(Scrapper.class.getName())
                .filter(t -> t.implementsInterface(SourceScrapper.class.getName())).forEach(t -> {
                    try {
                        sct.add((SourceScrapper) t.getConstructorInfo().filter(f -> {
                            if (f.getParameterInfo().length == 1) {
                                return f.getParameterInfo()[0].getTypeSignatureOrTypeDescriptor().toString()
                                        .equals(SourceManager.class.getName());
                            }
                            return false;
                        }).get(0).loadClassAndGetConstructor().newInstance(sourceManager));
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void prime() {
        sct.forEach(t -> genreManager.addAll(t.getAllGenre()));
    }

    public void run() {

        LinkedBlockingQueue<MangaPage> pgs = new LinkedBlockingQueue<>();

        sct.stream().unordered().map(t -> new Thread(() -> {
            int x = t.getCompleteNumberOfPages();
            for (int i = 1; i <= x; i++) {
                pgs.add(new MangaPage(i, t));
            }
        })).forEach(t -> t.start());

        long t0 = System.currentTimeMillis();
        Counter c = new Counter();

        Thread tl = new Thread(() -> {
            while (true) {
                System.out.println("Processed " + c.get() + " manga till now in "
                        + (System.currentTimeMillis() - t0) / 1000 + " seconds");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        tl.setDaemon(true);
        tl.start();

        while (pgs.size() > 0) {
            this.runExecutors.execute(() -> {
                MangaPage x = pgs.poll();
                SourceScrapper sc = x.getScrapper();
                try {
                    sc.getLiterallyEveryLink(x.getPage(), t -> {
                        if (mangaManager.isAlreadyProcessed(t)) {
                            System.out.println(t + ": Already Processed");
                            return;
                        }
                        System.out.println(t + ": Processing " + x);
                        MangaDTO mn = null;
                        try {
                            long t1 = System.currentTimeMillis();
                            mn = sc.getManga(t);
                            long t2 = System.currentTimeMillis();
                            System.out.println(t + ": Processed " + mn.getPrimaryTitle() + " having "
                                    + mn.getChapters().size() + " chapters in " + ((t2 - t1) / 1000) + " seconds");
                        } catch (MangaFetchingException e) {
                            e.printStackTrace();
                            mn = e.getPartialManga();
                            problemChildManager.insert(e.getURL());
                            System.out.println("Inserted Problem " + e.getURL());
                        }
                        if (mn != null) {
                            mn.getChapters().forEach(f -> f.setWatchTime(System.currentTimeMillis()));
                            mangaManager.insert(parse(mn));
                            System.out.println("Inserted " + mn.getPrimaryTitle());
                            c.increment();
                        }
                    });
                    System.out.println("Processed page: " + x);
                } catch (PageProcessingException p) {
                    p.printStackTrace();
                }
            });
        }
    }

    public void watch() {
        watchInternal(1);
    }

    private void watchInternal(int count) {
        this.callbackExecutor.submit(() -> {
            ListenableFuture<List<Object>> lf = Futures.successfulAsList(watch(count % 4 == 0 ? WatchMode.DEEP : WatchMode.SHALLOW));
            Futures.addCallback(lf, new FutureCallback<List<Object>>() {
                @Override
                public void onSuccess(@NullableDecl List<Object> result) {
                    try {
                        Thread.sleep(1000 * 60 * 7);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    watchInternal(count + 1);
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                    try {
                        Thread.sleep(1000 * 60 * 7);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    watchInternal(count + 1);
                }
            }, callbackExecutor);
        });
    }

    private List<? extends ListenableFuture<?>> watch(WatchMode watchMode) {

        this.watching = true;

        LinkedBlockingQueue<MangaPage> pgs = new LinkedBlockingQueue<>();

        sct.forEach(t -> new Thread(() -> {
            int x = t.getNumberOfPagesToWatch();
            for (int i = 1; i <= x; i++) {
                pgs.add(new MangaPage(i, t));
            }
        }).start());

        Counter prMn = new Counter();
        long t0 = System.currentTimeMillis();

        Thread tl = new Thread(() -> {
            while (true) {
                System.out.println("Processed " + prMn.get() + " manga till now in "
                        + (System.currentTimeMillis() - t0) / 1000 + " seconds");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        tl.setDaemon(true);
        tl.start();

        return pgs.stream().unordered().map(t -> this.watchExecutors.submit(() -> doWatch(watchMode, pgs, prMn))).toList();
    }

    private void doWatch(WatchMode watchMode, LinkedBlockingQueue<MangaPage> pgs, Counter prMn) {
        try {
            MangaPage x = pgs.take();
            SourceScrapper sc = x.getScrapper();
            try {
                sc.watch(x.getPage(), t -> {
                    if (!watching) {
                        return;
                    }
                    MangaDTO dto = null;
                    System.out.println("Watching: " + t);
                    try {
                        long t1 = System.currentTimeMillis();
                        dto = sc.getManga(t);
                        long t2 = System.currentTimeMillis();
                        System.out.println("Parsed " + dto.getPrimaryTitle() + " having "
                                + dto.getChapters().size() + " chapters in " + (t2 - t1 / 1000) + " seconds");
                        // check for existence
                    } catch (MangaFetchingException e) {
                        e.printStackTrace();
                        problemChildManager.insert(e.getURL());
                        System.out.println("Happened with problem : " + e.getURL());
                    }
                    if (dto != null) {
                        Manga l = mangaManager.getMangaByURL(t);
                        boolean a = metadataEqualsOnly(l, dto);
                        boolean b = chapterListEqualsOnly(l, dto);
                        boolean same = a || b;
                        if (same) {
                            if (watchMode == WatchMode.SHALLOW) {
                                watching = true;
                                System.out.println("Encountered duplicate in shallow mode... shutting down");
                            } else if (watchMode == WatchMode.DEEP) {
                                System.out.println("Encountered duplicate in deep mode... doing nothing");
                            }
                            return;
                        } else {
                            dto.getChapters().forEach(g -> {
                                if (g.getWatchTime() == null) {
                                    g.setWatchTime(System.currentTimeMillis());
                                }
                            });
                            mangaManager.update(parse(dto));
                            System.out.println("Updated: " + dto.getPrimaryTitle());
                            if (!b) {
                                try {
                                    Message m = Message.builder().setTopic(l.getId()).setNotification(Notification.builder().setTitle("Manga Update").setBody(dto.getPrimaryTitle() + " has been updated").build()).build();
                                    FirebaseMessaging.getInstance().send(m);
                                } catch (FirebaseMessagingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        prMn.increment();
                    }
                });
            } catch (PageProcessingException p) {
                p.printStackTrace();
            }
            System.out.println("Processed page: " + x);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean metadataEqualsOnly(Manga m, MangaDTO md) {
        return m.getName().equals(md.getPrimaryTitle()) && m.getUrl().equals(md.getURL())
                && m.getCoverURL().equals(md.getCoverURL()) && m.getDescription().equals(md.getDescription())
                && m.getSource().equals(md.getSource()) && m.isListed() && m.getStatus().equalsIgnoreCase(md.getStatus())
                && listEquals(m.getGenres(), md.getGenres(), (g, s) -> g.getName().equalsIgnoreCase(s))
                && listEquals(m.getAuthors(), md.getAuthors(), (a, s) -> a.getName().equalsIgnoreCase(s))
                && listEquals(m.getArtists(), md.getArtists(), (a, s) -> a.getName().equalsIgnoreCase(s));
    }

    private boolean chapterListEqualsOnly(Manga m, MangaDTO md) {
        return listEquals(m.getChapters(), md.getChapters(), this::chapterEquals);
    }

    private boolean chapterEquals(Chapter c, ChapterDTO cd) {
        return c.getChapterName().equals(cd.getChapterName()) && c.getChapterNumber().equals(cd.getChapterNumber())
                && c.getSequenceNumber().equals(cd.getSequenceNumber())
                && c.getUpdatedAt().toInstant().equals(cd.getUpdatedAt())
                && listEquals(c.getImagesURL(), cd.getImagesURL(), (p, s) -> p.getUrl().equals(s));
    }

    private <T, U> boolean listEquals(List<T> firstList, List<U> secondList, BiPredicate<T, U> equality) {
        try {
            if (firstList.size() != secondList.size()) {
                return false;
            }
            for (int i = 0; i < firstList.size(); i++) {
                if (!equality.test(firstList.get(i), secondList.get(i))) {
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    private Manga parse(MangaDTO rec) {
        Manga m = new Manga();
        m.setName(rec.getPrimaryTitle());
        m.setUrl(rec.getURL());
        m.setCoverURL(rec.getCoverURL());
        m.setDescriptionSmall(rec.getDescription().substring(0, Math.min(rec.getDescription().length(), 255)));
        m.setDescription(rec.getDescription());
        m.setSource(rec.getSource());
        m.setListed(true);
        m.setLastUpdated(Optional.ofNullable(rec.getLastUpdated()).map(t -> Timestamp.from(t)).orElse(null));
        m.setStatus(rec.getStatus());
        m.setGenres(rec.getGenres().stream().map(t -> genreManager.getGenre(t.toLowerCase())).toList());
        m.setAuthors(rec.getAuthors().stream().map(t -> authorManager.getAuthor(t.toLowerCase())).toList());
        m.setArtists(rec.getArtists().stream().map(t -> authorManager.getAuthor(t.toLowerCase())).toList());
        m.setChapters(rec.getChapters().stream().map(Chapter::new).toList());
        m.setLinkedId(titleManager.add(rec.getTitles(), rec.getSource()));
        return m;
    }

}

class Counter {

    private int i;

    public Counter() {
        this.i = 0;
    }

    public int get() {
        return this.i;
    }

    public void increment() {
        this.i++;
    }

}
