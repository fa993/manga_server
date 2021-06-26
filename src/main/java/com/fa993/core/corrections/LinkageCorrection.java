package com.fa993.core.corrections;

import javax.persistence.*;

@Entity
@Table(name = "linkage_correction")
public class LinkageCorrection {

    @Id
    @Column(name = "linkage_correction_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "url_from")
    private String urlFrom;

    @Column(name = "url_to")
    private String urlTo;

    @Column(name = "linkage_type")
    private LinkageType type;

    public LinkageCorrection() {
    }

    public LinkageCorrection(int id, String urlFrom, String urlTo, LinkageType type) {
        this.id = id;
        this.urlFrom = urlFrom;
        this.urlTo = urlTo;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrlFrom() {
        return urlFrom;
    }

    public void setUrlFrom(String urlFrom) {
        this.urlFrom = urlFrom;
    }

    public String getUrlTo() {
        return urlTo;
    }

    public void setUrlTo(String urlTo) {
        this.urlTo = urlTo;
    }

    public LinkageType getType() {
        return type;
    }

    public void setType(LinkageType type) {
        this.type = type;
    }
}
