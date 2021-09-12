package com.fa993.core.pojos;

import com.fa993.core.dto.MangaHeading;

import java.util.List;

public class MangaQueryResponse {

    private MangaQuery query;

    private List<MangaHeading> headings;

    public MangaQueryResponse(MangaQuery query, List<MangaHeading> headings) {
        this.query = query;
        this.headings = headings;
    }

    public MangaQuery getQuery() {
        return query;
    }

    public void setQuery(MangaQuery query) {
        this.query = query;
    }

    public List<MangaHeading> getHeadings() {
        return headings;
    }

    public void setHeadings(List<MangaHeading> headings) {
        this.headings = headings;
    }
}
