package com.fa993.home;

import com.fa993.core.dto.HomePageRoot;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class FileParser {

    public HomePageRoot parse() throws IOException {
        ObjectMapper obm = new ObjectMapper();
        obm.registerSubtypes(HomePageRoot.class);
        return obm.readValue(new File("/src/main/resources/home.json"), HomePageRoot.class);
    }

}
