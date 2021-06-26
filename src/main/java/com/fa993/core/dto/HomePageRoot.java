package com.fa993.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class HomePageRoot {

    @JsonProperty("title")
    private String title;

    @JsonProperty("designator")
    private String designator;

    @JsonProperty("children")
    private List<HomePageRoot> children;

    @JsonProperty("data")
    private List<MangaHeading> data;

    public HomePageRoot() {
    }

    public HomePageRoot(String title, String designator, List<HomePageRoot> children, List<MangaHeading> data) {
        this.title = title;
        this.designator = designator;
        this.children = children;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesignator() {
        return designator;
    }

    public void setDesignator(String designator) {
        this.designator = designator;
    }

    public List<HomePageRoot> getChildren() {
        return children;
    }

    public void setChildren(List<HomePageRoot> children) {
        this.children = children;
    }

    public List<MangaHeading> getData() {
        return data;
    }

    public void setData(List<MangaHeading> data) {
        this.data = data;
    }
}
