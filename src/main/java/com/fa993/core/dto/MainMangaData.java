package com.fa993.core.dto;

import com.fa993.core.pojos.Author;
import com.fa993.core.pojos.Genre;
import com.fa993.core.pojos.Source;

import java.sql.Timestamp;
import java.util.List;

public interface MainMangaData {

    public String getId();

    public String getLinkedId();

    public String getName();

    public String getCoverURL();

    public SourceData getSource();

    public List<ChapterData> getChapters();

    public List<Author> getAuthors();

    public List<Author> getArtists();

    public Timestamp getLastUpdated();

    public String getDescription();

    public List<Genre> getGenres();

    public String getStatus();

}
