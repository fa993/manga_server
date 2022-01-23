package com.fa993.core.pojos;

import javax.persistence.*;

@Entity
@Table(name = "manga_listing")
public class MangaListing {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manga_listing_id")
    private Integer id;

    @Column(name = "manga_id")
    private String mangaId;

    @Column(name = "publicId")
    private String publicId;

    @Column(name = "name")
    private String name;

    @Column(name = "cover_url")
    private String coverURL;

    @Column(name = "description_small")
    private String descriptionSmall;

    @Column(name = "genres")
    private String genres;

    public MangaListing() {
    }

    public MangaListing(String mangaId, String publicId) {
        this.mangaId = mangaId;
        this.publicId = publicId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMangaId() {
        return mangaId;
    }

    public void setMangaId(String mangaId) {
        this.mangaId = mangaId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    public String getDescriptionSmall() {
        return descriptionSmall;
    }

    public void setDescriptionSmall(String descriptionSmall) {
        this.descriptionSmall = descriptionSmall;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }
}
