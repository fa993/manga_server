package com.fa993.core.dto;

import java.sql.Timestamp;
import java.util.List;

public interface LinkedMangaData {

    public String getId();

    public String getLinkedId();

    public String getName();

    public String getCoverURL();

    public SourceData getSource();

    public List<ChapterData> getChapters();

    public Timestamp getLastUpdated();

}
