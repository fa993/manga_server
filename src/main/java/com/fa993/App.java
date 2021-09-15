package com.fa993;

import com.fa993.core.managers.*;
import com.fa993.web.MultiThreadScrapper;
import com.google.firebase.FirebaseApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements ApplicationRunner {

    @Autowired
    MangaManager mangaManager;

    @Autowired
    SourceManager sourceManager;

    @Autowired
    AuthorManager authorManager;

    @Autowired
    GenreManager genreManager;

    @Autowired
    PageManager pageManager;

    @Autowired
    TitleManager titleManager;

    @Autowired
    ProblemChildManager problemChildManager;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        genreManager.registerError("not available");
//      FirebaseApp.initializeApp(); //will test on compute engine
        MultiThreadScrapper scp = new MultiThreadScrapper(mangaManager, sourceManager, authorManager, genreManager, pageManager, titleManager, problemChildManager);
        scp.prime();
//        scp.watch();
        scp.run();
    }
}
