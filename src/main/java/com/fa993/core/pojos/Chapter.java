package com.fa993.core.pojos;

import com.fa993.retrieval.pojos.ChapterDTO;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chapter")
public class Chapter {

    @Id
    @Column(name = "chapter_id")
    private String id;

    @Column(name = "manga_id")
    private String mangaId;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Column(name = "chapter_name")
    private String chapterName;

    @Column(name = "chapter_number")
    private String chapterNumber;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "chapter_id", referencedColumnName = "chapter_id")
    private List<Page> imagesURL;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public Chapter() {
    }

    public Chapter(String id, String mangaId, Integer sequenceNumber, String chapterName, String chapterNumber, List<String> imagesURL, Instant updatedAt) {
        this.id = id;
        this.mangaId = mangaId;
        this.sequenceNumber = sequenceNumber;
        this.chapterName = chapterName;
        this.chapterNumber = chapterNumber;
        this.imagesURL = new ArrayList<>();
        for(int i = 0; i < imagesURL.size(); i++){
            this.imagesURL.add(new Page(i, imagesURL.get(i)));
        }
        this.updatedAt = updatedAt == null ? null : Timestamp.from(updatedAt);
    }

    public Chapter(String mangaId, ChapterDTO rec) {
        this(null, mangaId, rec.getSequenceNumber(), rec.getChapterName(), rec.getChapterNumber(), rec.getImagesURL(), rec.getUpdatedAt());
    }

    public Chapter(ChapterDTO rec) {
        this(null, rec);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMangaId() {
        return mangaId;
    }

    public void setMangaId(String mangaId) {
        this.mangaId = mangaId;
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

    public List<Page> getImagesURL() {
        return imagesURL;
    }

    public void setImagesURL(List<Page> imagesURL) {
        this.imagesURL = imagesURL;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "id='" + id + '\'' +
                ", mangaId='" + mangaId + '\'' +
                ", chapterName='" + chapterName + '\'' +
                ", chapterNumber='" + chapterNumber + '\'' +
                ", imagesURL=" + imagesURL +
                ", updatedAt=" + updatedAt +
                '}';
    }
}