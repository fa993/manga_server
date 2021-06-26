package com.fa993.retrieval.pojos;

import com.fa993.core.pojos.Source;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MangaDTO {

    private String primaryTitle;
    private List<String> titles;
    private String URL;
    private String coverURL;
    private List<ChapterDTO> chapters;
    private List<String> authors;
    private List<String> artists;
    private Instant lastUpdated;
    private String description;
    private List<String> genres;
    private String status;
    private Source source;

    public MangaDTO(String primaryTitle, List<String> titles, String URL, String coverURL,
                    List<ChapterDTO> chapters, List<String> authors, List<String> artists,
                    Instant lastUpdated, String description, List<String> genres,
                    String status, Source source) {
        this.primaryTitle = primaryTitle;
        this.titles = titles;
        this.URL = URL;
        this.coverURL = coverURL;
        this.chapters = chapters;
        this.authors = authors;
        this.artists = artists;
        this.lastUpdated = lastUpdated;
        this.description = description;
        this.genres = genres;
        this.status = status;
        this.source = source;
    }

    public MangaDTO() {
        this.titles = new ArrayList<>();
        this.artists = new ArrayList<>();
        this.authors = new ArrayList<>();
        this.chapters = new ArrayList<>();
        this.genres = new ArrayList<>();
    }

    public String getPrimaryTitle() {
        return primaryTitle;
    }

    public void setPrimaryTitle(String primaryTitle) {
        this.primaryTitle = primaryTitle;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    public List<ChapterDTO> getChapters() {
        return chapters;
    }

    public void setChapters(List<ChapterDTO> chapters) {
        this.chapters = chapters;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getArtists() {
        return artists;
    }

    public void setArtists(List<String> artists) {
        this.artists = artists;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MangaDTO) obj;
        return Objects.equals(this.primaryTitle, that.primaryTitle) &&
                Objects.equals(this.titles, that.titles) &&
                Objects.equals(this.URL, that.URL) &&
                Objects.equals(this.coverURL, that.coverURL) &&
                Objects.equals(this.chapters, that.chapters) &&
                Objects.equals(this.authors, that.authors) &&
                Objects.equals(this.artists, that.artists) &&
                Objects.equals(this.lastUpdated, that.lastUpdated) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.genres, that.genres) &&
                Objects.equals(this.status, that.status) &&
                Objects.equals(this.source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryTitle, titles, URL, coverURL, chapters, authors, artists, lastUpdated, description, genres, status, source);
    }

    @Override
    public String toString() {
        return "MangaDTO[" +
                "primaryTitle=" + primaryTitle + ", " +
                "titles=" + titles + ", " +
                "URL=" + URL + ", " +
                "coverURL=" + coverURL + ", " +
                "chapters=" + chapters + ", " +
                "authors=" + authors + ", " +
                "artists=" + artists + ", " +
                "lastUpdated=" + lastUpdated + ", " +
                "description=" + description + ", " +
                "genres=" + genres + ", " +
                "status=" + status + ", " +
                "source=" + source + ']';
    }


}
