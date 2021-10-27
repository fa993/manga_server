package com.fa993;

import com.fa993.retrieval.MultiThreadScrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class App implements ApplicationRunner {


    @Autowired
    private MultiThreadScrapper scp;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        FirebaseApp.initializeApp(); //will test on compute engine
        scp.run();
    }
}
