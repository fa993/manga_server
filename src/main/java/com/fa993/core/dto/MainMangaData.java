package com.fa993.core.dto;

import java.sql.Timestamp;
import java.util.List;

public interface MainMangaData {

    public String getId();

    public String getLinkedId();

    public String getName();

    public String getCoverURL();

    public SourceData getSource();

    public List<ChapterData> getChapters();

    public List<AuthorData> getAuthors();

    public List<AuthorData> getArtists();

    public Timestamp getLastUpdated();

    public String getDescription();

    public List<GenreData> getGenres();

    public String getStatus();

}
