package com.fa993.core.dto;

public class PageURLImpl implements PageURL {

    private String url;

    public PageURLImpl(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
