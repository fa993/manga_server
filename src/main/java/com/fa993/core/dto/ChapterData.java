package com.fa993.core.dto;

import java.sql.Timestamp;

public interface ChapterData {

    public String getId();
    public Integer getSequenceNumber();
    public String getChapterName();
    public String getChapterNumber();
    public Timestamp getUpdatedAt();

}
