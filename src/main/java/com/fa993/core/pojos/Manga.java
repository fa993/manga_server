package com.fa993.core.pojos;

import com.fa993.utils.Utility;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "manga")
public class Manga {

    @Id
    @Column(name = "manga_id")
    private String id;

    @Column(name = "linked_id")
    private String linkedId;

    @Column(name = "name")
    private String name;

    @Column(name = "cover_url")
//    @ColumnTransformer(forColumn = "cover_url", read = "UNCOMPRESS(cover_url)", write = "COMPRESS(?)")
    private String coverURL;

    @Column(name = "url")
//    @ColumnTransformer(forColumn = "url", read = "UNCOMPRESS(url)", write = "COMPRESS(?)")
    private String url;

    @Column(name = "is_listed")
    private boolean listed;

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

    @ManyToMany
    @JoinTable(
            name = "manga_genre",
            joinColumns = @JoinColumn(name = "manga_id", referencedColumnName = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "genre_id")
    )
    private List<Genre> genres;

    @Column(name = "status")
    private String status;

    @Column(name = "is_main")
    private Boolean main;

    public static Manga fromURL(String url) {
        Manga m = new Manga();
        m.setUrl(url);
        return m;
    }

    public Manga() {
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
        return listed;
    }

    public void setListed(boolean listed) {
        this.listed = listed;
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

    public Boolean isMain() {
        return main;
    }

    public void setMain(Boolean main) {
        this.main = main;
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
                ", isListed=" + listed +
                ", source=" + source +
                ", chapters=" + chapters +
                ", authors=" + authors +
                ", artists=" + artists +
                ", lastUpdated=" + lastUpdated +
                ", description='" + description + '\'' +
                ", genres=" + genres +
                ", status='" + status + '\'' +
                ", isMain=" + main +
                '}';
    }

}
