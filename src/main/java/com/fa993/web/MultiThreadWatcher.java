package com.fa993.web;

import com.fa993.core.managers.*;
import com.fa993.retrieval.Scrapper;
import com.fa993.retrieval.SourceScrapper;
import com.fa993.retrieval.pojos.MangaDTO;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class MultiThreadWatcher {

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

    public MultiThreadWatcher(MangaManager mangaManager, SourceManager sourceManager, AuthorManager authorManager, GenreManager genreManager, PageManager pageManager, TitleManager titleManager, ProblemChildManager problemChildManager) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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



    }

}
