package com.fa993.retrieval.pojos;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ChapterDTO {

    private int sequenceNumber;
    private String chapterName;
    private String chapterNumber;
    private List<String> imagesURL;
    private Instant updatedAt;

    public ChapterDTO(Integer sequenceNumber, String chapterName, String chapterNumber, List<String> imagesURL, Instant updatedAt) {
        this.sequenceNumber = sequenceNumber;
        this.chapterName = chapterName;
        this.chapterNumber = chapterNumber;
        this.imagesURL = imagesURL;
        this.updatedAt = updatedAt;
    }

    public ChapterDTO() {
        this.imagesURL = new ArrayList<>();
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(String chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public List<String> getImagesURL() {
        return imagesURL;
    }

    public void setImagesURL(List<String> imagesURL) {
        this.imagesURL = imagesURL;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChapterDTO) obj;
        return Objects.equals(this.chapterName, that.chapterName) &&
                Objects.equals(this.chapterNumber, that.chapterNumber) &&
                Objects.equals(this.imagesURL, that.imagesURL) &&
                Objects.equals(this.updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chapterName, chapterNumber, imagesURL, updatedAt);
    }

    @Override
    public String toString() {
        return "ChapterDTO[" +
                "chapterName=" + chapterName + ", " +
                "chapterNumber=" + chapterNumber + ", " +
                "imagesURL=" + imagesURL + ", " +
                "updatedAt=" + updatedAt + ']';
    }


}
