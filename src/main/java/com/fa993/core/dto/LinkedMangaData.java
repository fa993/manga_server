package com.fa993.core.dto;

import com.fa993.core.pojos.*;

import java.util.List;

public interface LinkedMangaData {

    public String getId();

    public String getName();

    public String getCoverURL();

    public SourceData getSource();

    public List<ChapterData> getChapters();

}
