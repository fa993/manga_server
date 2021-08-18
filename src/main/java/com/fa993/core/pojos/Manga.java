package com.fa993.core.pojos;

import com.fa993.utils.Utility;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "manga")
public class Manga {

    @JsonView(Views.Heading.class)
    @Id
    @Column(name = "manga_id")
    private String id;

    @Column(name = "linked_id")
    private String linkedId;

    @JsonView(Views.Heading.class)
    @Column(name = "name")
    private String name;

    @JsonView(Views.Heading.class)
    @Column(name = "cover_url")
    private String coverURL;

    @Column(name = "url")
    private String url;

    @Column(name = "is_listed")
    private boolean isListed;

    @ManyToOne
    @JoinColumn(name = "source_id", referencedColumnName = "source_id")
    private Source source;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "manga_id", referencedColumnName = "manga_id")
    private List<Chapter> chapters;

    @ManyToMany
    @JoinTable(
            name = "manga_author",
            joinColumns = @JoinColumn(name = "manga_id", referencedColumnName = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id", referencedColumnName = "author_id")
    )
    private List<Author> authors;

    @ManyToMany
    @JoinTable(
            name = "manga_artist",
            joinColumns = @JoinColumn(name = "manga_id", referencedColumnName = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id", referencedColumnName = "author_id")
    )
    private List<Author> artists;

    @Column(name = "last_updated")
    private Timestamp lastUpdated;

    @Column(name = "description")
    private String description;

    @JsonView(Views.Heading.class)
    @Column(name = "description_small")
    private String descriptionSmall;

    @JsonView(Views.PrettyHeading.class)
    @ManyToMany
    @JoinTable(
            name = "manga_genre",
            joinColumns = @JoinColumn(name = "manga_id", referencedColumnName = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "genre_id")
    )
    private List<Genre> genres;

    @Column(name = "status")
    private String status;

    public static Manga fromURL(String url) {
        Manga m = new Manga();
        m.setUrl(url);
        return m;
    }

    public Manga() {
    }

    public Manga(String id, String name, String coverURL, String descriptionSmall) {
        this.id = id;
        this.name = name;
        this.coverURL = coverURL;
        this.descriptionSmall = descriptionSmall;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLinkedId() {
        return linkedId;
    }

    public void setLinkedId(String linkedId) {
        this.linkedId = linkedId;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isListed() {
        return isListed;
    }

    public void setListed(boolean listed) {
        isListed = listed;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public List<Author> getArtists() {
        return artists;
    }

    public void setArtists(List<Author> artists) {
        this.artists = artists;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionSmall() {
        return descriptionSmall;
    }

    public void setDescriptionSmall(String descriptionSmall) {
        this.descriptionSmall = descriptionSmall;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = Utility.getID();
        }
    }

    @Override
    public String toString() {
        return "Manga{" +
                "id='" + id + '\'' +
                ", linkedId='" + linkedId + '\'' +
                ", name='" + name + '\'' +
                ", coverURL='" + coverURL + '\'' +
                ", url='" + url + '\'' +
                ", isListed=" + isListed +
                ", source=" + source +
                ", chapters=" + chapters +
                ", authors=" + authors +
                ", lastUpdated=" + lastUpdated +
                ", description='" + description + '\'' +
                ", descriptionSmall='" + descriptionSmall + '\'' +
                ", genres=" + genres +
                ", status='" + status + '\'' +
                '}';
    }
}