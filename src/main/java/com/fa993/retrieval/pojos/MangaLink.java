package com.fa993.retrieval.pojos;

import com.fa993.retrieval.SourceScrapper;

public class MangaLink {

    private String url;
    private SourceScrapper scrapper;

    public MangaLink(String url, SourceScrapper scrapper) {
        this.url = url;
        this.scrapper = scrapper;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SourceScrapper getScrapper() {
        return scrapper;
    }

    public void setScrapper(SourceScrapper scrapper) {
        this.scrapper = scrapper;
    }

    @Override
    public String toString() {
        return "MangaLink{" +
                "url='" + url + '\'' +
                ", scrapper=" + scrapper +
                '}';
    }
}
