package com.fa993.core.pojos;

import com.fa993.core.dto.MangaHeading;

import java.util.Collection;

public class MangaQueryResponse {

    private MangaQuery query;

    private Collection<MangaHeading> manga;

    public MangaQueryResponse(MangaQuery query, Collection<MangaHeading> manga) {
        this.query = query;
        this.manga = manga;
    }

    public MangaQueryResponse() {
    }

    public MangaQuery getQuery() {
        return query;
    }

    public void setQuery(MangaQuery query) {
        this.query = query;
    }

    public Collection<MangaHeading> getManga() {
        return manga;
    }

    public void setManga(Collection<MangaHeading> manga) {
        this.manga = manga;
    }
}
