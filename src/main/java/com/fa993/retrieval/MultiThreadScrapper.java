package com.fa993.retrieval;

import com.fa993.core.exceptions.MangaFetchingException;
import com.fa993.core.managers.*;
import com.fa993.core.pojos.Chapter;
import com.fa993.core.pojos.Manga;
import com.fa993.core.pojos.Source;
import com.fa993.retrieval.pojos.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class MultiThreadScrapper {

    Logger logger = LoggerFactory.getLogger(MultiThreadScrapper.class);

    private final int runThreads;
    private final int watchThreads;

    private final List<SourceScrapper> sct;

    private final MangaManager mangaManager;
    private final SourceManager sourceManager;
    private final AuthorManager authorManager;
    private final GenreManager genreManager;
    private final PageManager pageManager;
    private final TitleManager titleManager;
    private final ChapterManager chapterManager;

    private final ProblemChildManager problemChildManager;

    private byte watchCount = 1;
    
    private volatile boolean running;

    private ExecutorService watchService;

    public MultiThreadScrapper(MangaManager mangaManager, SourceManager sourceManager, AuthorManager authorManager,
                               GenreManager genreManager, PageManager pageManager, TitleManager titleManager, ChapterManager chapterManager,
                               ProblemChildManager problemChildManager) {
        genreManager.registerError("not available");
        this.mangaManager = mangaManager;
        this.sourceManager = sourceManager;
        this.authorManager = authorManager;
        this.genreManager = genreManager;
        this.pageManager = pageManager;
        this.titleManager = titleManager;
        this.chapterManager = chapterManager;
        this.problemChildManager = problemChildManager;
        this.runThreads = 4;//32
        this.watchThreads = 4;//32
        ScanResult rs = new ClassGraph().acceptPackages(this.getClass().getPackageName()).enableAllInfo().scan();
        List<SourceScrapper> tmp = new ArrayList<>();
        rs.getClassesWithAnnotation(Scrapper.class.getName())
                .filter(t -> t.implementsInterface(SourceScrapper.class.getName())).forEach(t -> {
                    try {
                        tmp.add((SourceScrapper) t.getConstructorInfo().filter(f -> {
                            if (f.getParameterInfo().length == 1) {
                                return f.getParameterInfo()[0].getTypeSignatureOrTypeDescriptor().toString()
                                        .equals(SourceManager.class.getName());
                            }
                            return false;
                        }).get(0).loadClassAndGetConstructor().newInstance(sourceManager));
                    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
        this.sct = Collections.unmodifiableList(tmp);
        this.watchService = Executors.newSingleThreadExecutor(r -> {
            Thread t0 = new Thread(r);
            t0.setDaemon(true);
            return t0;
        });
        this.prime();
    }

    public void prime() {
        sct.forEach(t -> genreManager.addAll(t.getAllGenre()));
    }

//    public void run() throws InterruptedException {
//
//        this.running = true;
//
//        ThreadPoolExecutor pageExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(runThreads, r -> {
//            Thread t = new Thread(r);
//            t.setDaemon(true);
//            return t;
//        });
//
//        ThreadPoolExecutor mangaExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(runThreads, r -> {
//            Thread t = new Thread(r);
//            t.setDaemon(true);
//            return t;
//        });
//
//        AtomicInteger c = new AtomicInteger(0);
//        long t0 = System.currentTimeMillis();
//
//        Thread tl = new Thread(() -> {
//            int cumulative = 0;
//            long l1 = System.currentTimeMillis();
//            while (!mangaExecutor.isTerminated()) {
//                int i1 = c.get();
//                c.set(0);
//                long l2 = System.currentTimeMillis();
//                long i2 = ((l2 - t0) / 1000);
//                long i3 = ((l2 - l1) / 1000);
//                l1 = l2;
//                cumulative += i1;
//                System.out.println("Processed " + cumulative + " manga till now in "
//                        + i2 + " seconds");
//                System.out.println("Manga Threads: " + runThreads);
//                if (i2 != 0) {
//                    System.out.println("Instantaneous Rate: " + ((double) i1 / i3));
//                    System.out.println("Cumulative Ratio: " + ((double) cumulative / i2));
//                }
//                System.out.println("Pages done: " + pageExecutor.getCompletedTaskCount());
//                System.out.println("Manga done: " + mangaExecutor.getCompletedTaskCount());
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        tl.setDaemon(true);
//        tl.start();
//
//        final int[] curr = new int[sct.size()];
//        this.sct.forEach(SourceScrapper::reloadCompletePages);
//        int tot = sct.stream().reduce(0, (o, i) -> o += i.getCompleteNumberOfPages(), Integer::sum);
//        for (int i = 0; i < tot; i++) {
//            int in = i % curr.length;
//            int x = curr[in];
//            SourceScrapper sc = sct.get(in);
//            if (x < sc.getCompleteNumberOfPages()) {
//                pageExecutor.submit(() -> {
//                    try {
//                        mangaExecutor.invokeAll(sc.getLiterallyEveryLink(x + 1).stream().map(t ->
//                                (Callable<Object>) () -> {
//                                    parseAndInsert(c, t, sc);
//                                    return null;
//                                }
//                        ).toList());
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                });
//                curr[in]++;
//            } else {
//                tot++;
//            }
//        }
//        pageExecutor.shutdown();
//        pageExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
//        mangaExecutor.shutdown();
//        mangaExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
//        this.running = false;
//    }

//    private void parseAndInsert(AtomicInteger c, String t, SourceScrapper sc) {
//        boolean b = problemChildManager.isProblem(t);
//        if (mangaManager.isAlreadyProcessed(t) && !problemChildManager.isProblem(t)) {
//            System.out.println(t + ": Already Processed");
//            return;
//        }
//        String toPrint = t + ": Processing " + t + " from " + sc.toString();
//        if(b) {
//            toPrint += " which is problem";
//        }
//        System.out.println(toPrint);
//        MangaDTO mn = null;
//        try {
//            long t1 = System.currentTimeMillis();
//            mn = sc.getManga(t);
//            long t2 = System.currentTimeMillis();
//            System.out.println(t + ": Processed " + mn.getPrimaryTitle() + " having "
//                    + mn.getChapters().size() + " chapters in " + ((t2 - t1) / 1000.0) + " seconds");
//        } catch (MangaFetchingException e) {
//            e.printStackTrace();
//            mn = e.getPartialManga();
//            problemChildManager.insert(e.getURL());
//            System.out.println("Inserted Problem " + e.getURL());
//        } finally {
//            if (mn != null) {
//                mn.getChapters().forEach(f -> f.setWatchTime(System.currentTimeMillis()));
//                boolean f = true;
//                while (f) {
//                    try {
//                        mangaManager.insert(parse(mn));
//                        f = false;
//                    } catch (CannotAcquireLockException ex) {
//                        System.out.println(mn.getTitles() + " deadlocked");
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                System.out.println("Inserted " + mn.getPrimaryTitle());
//                c.incrementAndGet();
//            }
//        }
//    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void watchForExistence() throws InterruptedException, ExecutionException{
        this.deleteOlds();
        if(this.running) {
            System.out.println("Currently Running so aborting watch");
            return;
        }

        ThreadPoolExecutor pageExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(watchThreads, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        ThreadPoolExecutor mangaExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(watchThreads, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        AtomicInteger c = new AtomicInteger(0);
        AtomicInteger f2 = new AtomicInteger(0);
        long t0 = System.currentTimeMillis();

        Thread tl = new Thread(() -> {
            int cumulative = 0;
            long l1 = System.currentTimeMillis();
            while (!mangaExecutor.isTerminated()) {
                int i1 = c.get();
                c.set(0);
                long l2 = System.currentTimeMillis();
                long i2 = ((l2 - t0) / 1000);
                long i3 = ((l2 - l1) / 1000);
                l1 = l2;
                cumulative += i1;
                System.out.println("Processed " + cumulative + " manga till now in "
                        + i2 + " seconds");
                System.out.println("Manga Threads: " + watchThreads);
                if (i2 != 0) {
                    System.out.println("Instantaneous Rate: " + ((double) i1 / i3));
                    System.out.println("Cumulative Ratio: " + ((double) cumulative / i2));
                }
                System.out.println("Manga Added: " + f2.get());
                System.out.println("Pages done: " + pageExecutor.getCompletedTaskCount());
                System.out.println("Manga done: " + mangaExecutor.getCompletedTaskCount());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        tl.setDaemon(true);
        tl.start();

        List<Future<Object>> tmp = new ArrayList<>();
        final int[] curr = new int[sct.size()];
        this.sct.forEach(SourceScrapper::reloadWatchPages);
        int tot = sct.stream().reduce(0, (o, i) -> o += i.getNumberOfPagesToWatch(), Integer::sum);
        for (int i = 0; i < tot; i++) {
            int in = i % curr.length;
            int x = curr[in];
            SourceScrapper sc = sct.get(in);
            if (x < sc.getNumberOfPagesToWatch()) {
                pageExecutor.submit(() -> {
                    tmp.addAll(mangaExecutor.invokeAll(sc.watch(x + 1).stream().map(t ->
                            (Callable<Object>) () -> {
                                try {
                                    if (!mangaExecutor.isShutdown()) {
                                        insertIfNotExists(t, c, f2, sc);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                } finally {
                                    return null;
                                }
                            }
                    ).toList()));
                    return null;
                });
                curr[in]++;
            } else {
                tot++;
            }
        }
        pageExecutor.shutdown();
        pageExecutor.awaitTermination(10 * 60 * 1000, TimeUnit.MILLISECONDS);
        for (Future<Object> f : tmp) {
            if(mangaExecutor.isShutdown()){
                break;
            }
            f.get();
        }
        mangaExecutor.shutdown();
        mangaExecutor.awaitTermination(10 * 60 * 1000, TimeUnit.MILLISECONDS);
        System.gc();
        System.out.println("Done Watching");
    }

    public void deleteOlds() {
        this.mangaManager.deleteOlds();
    }

    private void insertIfNotExists(String t, AtomicInteger c, AtomicInteger f, SourceScrapper sc) {
        if(!this.mangaManager.isAlreadyProcessed(t)){
            doWatchSingle(t, sc.getSource().getId(), f);
        }
        c.incrementAndGet();
    }

//    @Scheduled(initialDelay = 5 * 60 * 1000,fixedDelay = 5 * 60 * 1000)
//    public void watch() throws InterruptedException, ExecutionException {
//
//        if(this.running) {
//            System.out.println("Currently Running so aborting watch");
//            return;
//        }
//
//        ThreadPoolExecutor pageExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(watchThreads, r -> {
//            Thread t = new Thread(r);
//            t.setDaemon(true);
//            return t;
//        });
//
//        ThreadPoolExecutor mangaExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(watchThreads, r -> {
//            Thread t = new Thread(r);
//            t.setDaemon(true);
//            return t;
//        });
//
////        WatchMode watchMode = (watchCount++) % 4 == 0 ? WatchMode.DEEP : WatchMode.SHALLOW;
//        WatchMode watchMode = WatchMode.SHALLOW;
//
//        AtomicInteger c = new AtomicInteger(0);
//        long t0 = System.currentTimeMillis();
//
//        Thread tl = new Thread(() -> {
//            int cumulative = 0;
//            long l1 = System.currentTimeMillis();
//            while (!mangaExecutor.isTerminated()) {
//                int i1 = c.get();
//                c.set(0);
//                long l2 = System.currentTimeMillis();
//                long i2 = ((l2 - t0) / 1000);
//                long i3 = ((l2 - l1) / 1000);
//                l1 = l2;
//                cumulative += i1;
//                System.out.println("Processed " + cumulative + " manga till now in "
//                        + i2 + " seconds");
//                System.out.println("Manga Threads: " + watchThreads);
//                if (i2 != 0) {
//                    System.out.println("Instantaneous Rate: " + ((double) i1 / i3));
//                    System.out.println("Cumulative Ratio: " + ((double) cumulative / i2));
//                }
//                System.out.println("Pages done: " + pageExecutor.getCompletedTaskCount());
//                System.out.println("Manga done: " + mangaExecutor.getCompletedTaskCount());
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        tl.setDaemon(true);
//        tl.start();
//
//        List<Future<Object>> tmp = new ArrayList<>();
//        final int[] curr = new int[sct.size()];
//        this.sct.forEach(SourceScrapper::reloadWatchPages);
//        int tot = sct.stream().reduce(0, (o, i) -> o += i.getNumberOfPagesToWatch(), Integer::sum);
//        for (int i = 0; i < tot; i++) {
//            int in = i % curr.length;
//            int x = curr[in];
//            SourceScrapper sc = sct.get(in);
//            if (x < sc.getNumberOfPagesToWatch()) {
//                pageExecutor.submit(() -> {
//                    tmp.addAll(mangaExecutor.invokeAll(sc.watch(x + 1).stream().map(t ->
//                            (Callable<Object>) () -> {
//                                if (!mangaExecutor.isShutdown()) {
//                                    parseAndWatch(c, t, sc, watchMode, () -> {
//                                        mangaExecutor.shutdownNow();
//                                        pageExecutor.shutdownNow();
//                                    }, mangaExecutor::isShutdown);
//                                }
//                                return null;
//                            }
//                    ).toList()));
//                    return null;
//                });
//                curr[in]++;
//            } else {
//                tot++;
//            }
//        }
//        pageExecutor.shutdown();
//        pageExecutor.awaitTermination(10 * 60 * 1000, TimeUnit.MILLISECONDS);
//        for (Future<Object> f : tmp) {
//            if(mangaExecutor.isShutdown()){
//                break;
//            }
//            f.get();
//        }
//        mangaExecutor.shutdown();
//        mangaExecutor.awaitTermination(10 * 60 * 1000, TimeUnit.MILLISECONDS);
//        System.gc();
//        System.out.println("Done Watching");
//    }
//
//    private void parseAndWatch(AtomicInteger c, String t, SourceScrapper sc, WatchMode watchMode, Runnable shutdown, Supplier<Boolean> isShutdown) {
//        MangaDTO dto = null;
//        System.out.println("Watching: " + t);
//        try {
//            long t1 = System.currentTimeMillis();
//            dto = sc.getManga(t);
//            long t2 = System.currentTimeMillis();
//            System.out.println("Parsed " + dto.getPrimaryTitle() + " having "
//                    + dto.getChapters().size() + " chapters in " + ((t2 - t1) / 1000) + " seconds");
//            // check for existence
//        } catch (MangaFetchingException e) {
//            e.printStackTrace();
//            problemChildManager.insert(e.getURL());
//            System.out.println("Happened with problem : " + e.getURL());
//        } finally {
//            if (dto != null) {
//                dto.getChapters().forEach(g -> {
//                    if (g.getWatchTime() == null) {
//                        g.setWatchTime(System.currentTimeMillis());
//                    }
//                });
//                String id = mangaManager.getId(t);
//                if (id == null) {
//                    Manga m2 = parse(dto);
//                    mangaManager.insert(m2);
//                    c.incrementAndGet();
//                    System.out.println("Inserted " + dto.getPrimaryTitle());
//                } else {
//                    Manga m1 = mangaManager.getManga(id);
//                    boolean a = metadataEqualsOnly(m1, dto);
//                    boolean b = chapterListEqualsOnly(m1, dto);
//                    boolean same = a && b;
//                    if (same) {
//                        if (watchMode == WatchMode.SHALLOW) {
//                            System.out.println("Encountered duplicate in shallow mode... shutting down");
//                            shutdown.run();
//                        } else if (watchMode == WatchMode.DEEP) {
//                            System.out.println("Encountered duplicate in deep mode... doing nothing");
//                        }
//                        return;
//                    } else {
//                        Manga m2 = parse(dto);
//                        m2.setPublicId(id);
//                        m2.setMain(m1.getMain());
//                        Manga l = mangaManager.insert(m2);
//                        System.out.println("Updated: " + dto.getPrimaryTitle());
//                        if (!b) {
//                            try {
//                                Message m = Message.builder().setTopic(l.getId()).setNotification(Notification.builder().setTitle("Manga Update").setBody(dto.getPrimaryTitle() + " has been updated").build()).build();
//                                FirebaseMessaging.getInstance().send(m);
//                            } catch (FirebaseMessagingException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        c.incrementAndGet();
//                    }
//                }
//            }
//        }
//    }

    public void watchSingle(String t, String sourceId) {
        this.watchService.submit(()->doWatchSingle(t, sourceId, null));
    }

    private void doWatchSingle(String t, String sourceId, AtomicInteger f) {
//        if(!this.mangaManager.isOld(System.currentTimeMillis(), ref2)) {
//            System.out.println("Timeout not elapsed");
//            return;
//        }
        MangaDTO dto = null;
        SourceScrapper sc = null;
        for(SourceScrapper s1 : this.sct) {
            if(s1.getSource().getId().equals(sourceId)){
                sc = s1;
            }
        }
        if(sc == null) {
            throw new RuntimeException("No matching Scrapper for given Source");
        }
        logger.info("Watching: " + t);
        try {
            long t1 = System.currentTimeMillis();
            dto = sc.getManga(t);
            long t2 = System.currentTimeMillis();
            System.out.println("Parsed " + dto.getPrimaryTitle() + " having "
                    + dto.getChapters().size() + " chapters in " + ((t2 - t1) / 1000) + " seconds");
            // check for existence
        } catch (MangaFetchingException e) {
            e.printStackTrace();
            problemChildManager.insert(e.getURL());
            System.out.println("Happened with problem : " + e.getURL());
            dto = e.getPartialManga();
        } finally {
            if (dto != null) {
                dto.getChapters().forEach(g -> {
                    if (g.getWatchTime() == null) {
                        g.setWatchTime(System.currentTimeMillis());
                    }
                });
                String id = mangaManager.getId(t);
                if (id == null) {
                    try {
                        Manga m2 = parse(dto);
                        mangaManager.insert(m2);
                        System.out.println("Inserted " + dto.getPrimaryTitle());
                        if(f != null) {
                            f.incrementAndGet();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Manga m1 = mangaManager.getManga(id);
                    boolean a = metadataEqualsOnly(m1, dto);
                    boolean b = chapterListEqualsOnly(m1, dto);
                    boolean same = a && b;
                    if (same) {
                        System.out.println("Manga are same doing nothing");
                        this.mangaManager.updateWatchTime(m1.getId(), System.currentTimeMillis());
                        return;
                    } else {
                        Manga m2 = parse(dto);
                        m2.setId(m1.getId());
                        m2.setPublicId(m1.getPublicId());
                        m2.setMain(m1.getMain());
                        m1.getChapters().sort(Comparator.comparingInt(Chapter::getSequenceNumber));
                        m2.getChapters().sort(Comparator.comparingInt(Chapter::getSequenceNumber));
                        if(m2.getChapters().size() < m1.getChapters().size()) {
                            //supremely rare case
                            //delete extra chapters
                            this.chapterManager.deleteChaps(m1.getChapters().subList(m2.getChapters().size(), m1.getChapters().size()).stream().map(Chapter::getId).toList());
                        }
                        for(int i = 0; i < Math.min(m2.getChapters().size(), m1.getChapters().size()); i++) {
                            m2.getChapters().get(i).setId(m1.getChapters().get(i).getId());
                        }
                        mangaManager.updateManga(m2);
                        System.out.println("Updated: " + dto.getPrimaryTitle());
                        if(f != null) {
                            f.incrementAndGet();
                        }
                        if (!b) {
                            try {
                                Message m = Message.builder().setTopic(m2.getId()).setNotification(Notification.builder().setTitle("Manga Update").setBody(dto.getPrimaryTitle() + " has been updated").build()).build();
                                FirebaseMessaging.getInstance().send(m);
                            } catch (FirebaseMessagingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private Manga parse(MangaDTO rec) {
        Manga m = new Manga();
        m.setName(rec.getPrimaryTitle());
        m.setUrl(rec.getURL());
        m.setCoverURL(rec.getCoverURL());
//        m.setDescriptionSmall(rec.getDescription().substring(0, Math.min(rec.getDescription().length(), 255)));
        m.setDescription(rec.getDescription());
        m.setSource(rec.getSource());
        m.setListed(true);
        m.setLastUpdated(Optional.ofNullable(rec.getLastUpdated()).map(Timestamp::from).orElse(null));
        m.setStatus(rec.getStatus());
        m.setGenres(rec.getGenres().stream().map(t -> genreManager.getGenre(t == null ? null : t.toLowerCase())).toList());
        m.setAuthors(rec.getAuthors().stream().map(t -> authorManager.getAuthor(t.toLowerCase())).toList());
        m.setArtists(rec.getArtists().stream().map(t -> authorManager.getAuthor(t.toLowerCase())).toList());
        m.setChapters(rec.getChapters().stream().map(Chapter::new).toList());
        m.setLinkedId(titleManager.add(rec.getTitles()));
        m.setLastWatchTime(System.currentTimeMillis());
        m.setOld(false);
        return m;
    }

    private boolean metadataEqualsOnly(Manga m, MangaDTO md) {
        if(m == null || md == null) {
            logger.info("Null Manga Found");
            return false;
        }
        return m.getName().equals(md.getPrimaryTitle()) && m.getUrl().equals(md.getURL())
                && m.getCoverURL().equals(md.getCoverURL()) && m.getDescription().equals(md.getDescription())
                && m.getSource().equals(md.getSource()) && m.getStatus().equalsIgnoreCase(md.getStatus())
                && m.getGenres().stream().map(t -> t.getName().toLowerCase()).collect(Collectors.toSet()).equals(md.getGenres().stream().map(String::toLowerCase).collect(Collectors.toSet()))
                && m.getAuthors().stream().map(t -> t.getName().toLowerCase()).collect(Collectors.toSet()).equals(md.getAuthors().stream().map(String::toLowerCase).collect(Collectors.toSet()))
                && m.getArtists().stream().map(t -> t.getName().toLowerCase()).collect(Collectors.toSet()).equals(md.getArtists().stream().map(String::toLowerCase).collect(Collectors.toSet()))
//                && listEquals(m.getGenres(), md.getGenres(), (g, s) -> g.getName().equalsIgnoreCase(s))
//                && listEquals(m.getAuthors(), md.getAuthors(), (a, s) -> a.getName().equalsIgnoreCase(s))
//                && listEquals(m.getArtists(), md.getArtists(), (a, s) -> a.getName().equalsIgnoreCase(s))
                ;
    }

    private boolean chapterListEqualsOnly(Manga m, MangaDTO md) {
        if(m == null || md == null) {
            logger.info("Null Manga Found");
            return false;
        }
        m.getChapters().sort(Comparator.comparingInt(Chapter::getSequenceNumber));
        md.getChapters().sort(Comparator.comparingInt(ChapterDTO::getSequenceNumber));
        return listEquals(m.getChapters(), md.getChapters(), this::chapterEquals);
    }

    private boolean chapterEquals(Chapter c, ChapterDTO cd) {
        if(c == null || cd == null) {
            logger.info("Null Chapter Found");
        }
        return c != null && cd != null && c.getChapterName().equals(cd.getChapterName()) && c.getChapterNumber().equals(cd.getChapterNumber())
                && c.getSequenceNumber().equals(cd.getSequenceNumber())
                && nullEquals(c.getUpdatedAt(), cd.getUpdatedAt(), (cx, cdx) -> nullEquals(cx.toInstant(), cdx, Instant::equals))
                && listEquals(c.getImagesURL(), cd.getImagesURL(), (p, s) -> p.getUrl().equals(s));
    }

    private <T, U> boolean nullEquals(T firstObject, U secondObject, BiPredicate<T, U> equality) {
        try {
            if (firstObject == null && secondObject == null) {
                return true;
            }
            return equality.test(firstObject, secondObject);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
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

}

