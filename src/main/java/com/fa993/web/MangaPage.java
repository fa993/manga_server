package com.fa993.web;

import com.fa993.retrieval.SourceScrapper;

public class MangaPage {

    private int page;
    private SourceScrapper scrapper;

    public MangaPage(int page, SourceScrapper scrapper) {
        this.page = page;
        this.scrapper = scrapper;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public SourceScrapper getScrapper() {
        return scrapper;
    }

    public void setScrapper(SourceScrapper scrapper) {
        this.scrapper = scrapper;
    }

    @Override
    public String toString() {
        return "MangaPage{" +
                "page=" + page +
                ", scrapper=" + scrapper +
                '}';
    }
}
