package com.fa993.web;

import com.fa993.core.managers.*;
import com.fa993.core.pojos.Chapter;
import com.fa993.core.pojos.Manga;
import com.fa993.retrieval.Scrapper;
import com.fa993.retrieval.SourceScrapper;
import com.fa993.retrieval.pojos.MangaDTO;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MultiThreadScrapper {

    private volatile int y;
    private volatile int processedManga;

    private final int pagesThreadCount;
    private final int linksThreadCount;

    private Queue<MangaDTO> allManga;

    private Queue<MangaLink> links;

    private Consumer<String> r;

    private final List<SourceScrapper> sct;

    private MangaManager mangaManager;

    private SourceManager sourceManager;

    private AuthorManager authorManager;

    private GenreManager genreManager;

    private PageManager pageManager;

    private TitleManager titleManager;

    private ProblemChildManager problemChildManager;

    private final Queue<String> problemURL;

    public MultiThreadScrapper(MangaManager mangaManager, SourceManager sourceManager, AuthorManager authorManager, GenreManager genreManager, PageManager pageManager, TitleManager titleManager, ProblemChildManager problemChildManager) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.mangaManager = mangaManager;
        this.sourceManager = sourceManager;
        this.authorManager = authorManager;
        this.genreManager = genreManager;
        this.pageManager = pageManager;
        this.titleManager = titleManager;
        this.problemChildManager = problemChildManager;
        this.y = 0;
        this.linksThreadCount = 16;
        this.pagesThreadCount = 16;
        this.links = new LinkedBlockingQueue<>();
        this.problemURL = new LinkedBlockingQueue<>();
        this.allManga = new LinkedList<>();
        ScanResult rs = new ClassGraph().acceptPackages(this.getClass().getPackageName()).enableAllInfo().scan();
        this.sct = new ArrayList<>();
        rs.getClassesWithAnnotation(Scrapper.class.getName()).filter(t -> t.implementsInterface(SourceScrapper.class.getName())).forEach(t -> {
            try {
                sct.add((SourceScrapper) t.getConstructorInfo().filter(f -> {
                    if (f.getParameterInfo().length == 1) {
                        return f.getParameterInfo()[0].getTypeSignatureOrTypeDescriptor().toString().equals(SourceManager.class.getName());
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
        this.sct.forEach(t -> t.acceptOnProblem(problemURL::add));
        this.processedManga = 0;
        this.r = (t) -> {
            while (y < pagesThreadCount || links.size() > 0) {
                MangaLink x = links.poll();
                try {
                    if (x == null) {
                        Thread.sleep(100);
                        continue;
                    }
                    if (mangaManager.isAlreadyProcessed(x.getUrl())) {
                        System.out.println(t + ": Already Processed");
                        continue;
                    }
                    System.out.println(t + ": Processing " + x);
                    long t1 = System.currentTimeMillis();
                    MangaDTO mn = x.getScrapper().getManga(x.getUrl());
                    allManga.add(mn);
                    processedManga++;
                    System.out.println(t + ": Processed " + mn.getPrimaryTitle() + " having " + mn.getChapters().size() + " chapters in " + ((System.currentTimeMillis() - t1) / 1000) + " seconds");
                } catch (Exception e) {
                    e.printStackTrace();
                    problemURL.add(x.getUrl());
                }
            }
            y++;
        };
    }

    public void run() {

        sct.forEach(t -> genreManager.addAll(t.getAllGenre()));

        Queue<MangaPage> pgs = new LinkedBlockingQueue<>();

        List<Thread> ts = sct.stream().unordered().map(t -> new Thread(() -> {
            int x = t.getNumberOfPages();
            for (int i = 1; i <= x; i++) {
                pgs.add(new MangaPage(i, t));
            }
        })).collect(Collectors.toList());

        ts.forEach(t -> t.start());

        System.out.println(pgs);


        for (int i = 0; i < pagesThreadCount; i++) {
            int finalI = i;
            new Thread(() -> {
                while (pgs.size() > 0) {
                    MangaPage x = pgs.poll();
                    x.getScrapper().getLiterallyEveryLink(x.getPage(), t -> links.add(new MangaLink(t, x.getScrapper())));
                    System.out.println("Thread " + finalI + ": Processed page " + x);
                }
                y++;
                r.accept("Thread " + finalI);
            }).start();
        }

        for (int i = 0; i < linksThreadCount; i++) {
            int finalI = i + pagesThreadCount;
            new Thread(() -> {
                r.accept("Thread " + finalI);
            }).start();
        }

        long t1 = System.currentTimeMillis();

        Thread t = new Thread(() -> {
            while (true) {
                System.out.println("Processed " + processedManga + " manga till now in " + (System.currentTimeMillis() - t1) / 1000 + " seconds");
                System.out.println("Backlog: " + allManga.size());
                System.out.println("Total Backlog: " + links.size());
                System.out.println("Y: " + y);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();

        new Thread(() -> {
            while (y < (pagesThreadCount + pagesThreadCount + linksThreadCount) || allManga.size() > 0 || links.size() > 0) {
                MangaDTO m = allManga.poll();
                try {
                    if (m == null) {
                        Thread.sleep(100);
                        continue;
                    }
                    mangaManager.insert(parse(m));
                    System.out.println("Inserted " + m.getPrimaryTitle());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Happened With: " + m);
                }
            }
        }).start();

        Thread t2 = new Thread(() -> {
            while (y < (pagesThreadCount + pagesThreadCount + linksThreadCount) || allManga.size() > 0 || links.size() > 0 || problemURL.size() > 0) {
                String x = problemURL.poll();
                try {
                    if (x == null) {
                        Thread.sleep(100);
                        continue;
                    }
                    problemChildManager.insert(x);
                    System.out.println("Inserted Problem " + x);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Happened With(Problem): " + x);
                }
            }
        });
        t2.start();
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
