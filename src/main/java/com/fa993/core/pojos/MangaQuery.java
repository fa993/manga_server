package com.fa993.core.pojos;


import java.util.List;

public class MangaQuery {

    private String id;

    private String name;

    private int offset;

    private int limit;

    private String preferredSourceId;

    private List<String> genreIds;


    public MangaQuery(String id, String name, int offset, int limit, String preferredSourceId, List<String> genreIds) {
        this.id = id;
        this.name = name;
        this.offset = offset;
        this.limit = limit;
        this.preferredSourceId = preferredSourceId;
        this.genreIds = genreIds;
    }

    public MangaQuery() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getPreferredSourceId() {
        return preferredSourceId;
    }

    public void setPreferredSourceId(String preferredSourceId) {
        this.preferredSourceId = preferredSourceId;
    }

    public List<String> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<String> genreIds) {
        this.genreIds = genreIds;
    }
}
