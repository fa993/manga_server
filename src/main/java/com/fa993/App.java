package com.fa993;

import com.fa993.retrieval.MultiThreadScrapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
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
        System.out.println(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
        FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).build();
        FirebaseApp.initializeApp(options); //will test on compute engine
//        scp.run();
//        scp.deleteOldsAndOrphaned();
    }
}
