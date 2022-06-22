package com.fa993.core.pojos;

import com.fa993.utils.Utility;

import javax.persistence.*;

@Entity
@Table(name = "source_pattern")
public class SourcePattern {

    @Id
    @Column(name = "source_pattern_id")
    private String id;

    @Column(name = "url")
    private String url;

    @Column(name = "source_id")
    private String sourceId;

    public SourcePattern() {
    }

    public SourcePattern(String id) {
        this.id = id;
    }

    public SourcePattern(String source_pattern_id, String url, String sourceId) {
        this.id = source_pattern_id;
        this.url = url;
        this.sourceId = sourceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = Utility.getID();
        }
    }
}
