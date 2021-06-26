package com.fa993.core.pojos;

import com.fa993.utils.Utility;

import javax.persistence.*;

@Entity
@Table(name = "title")
public class Title {

    @Id
    @Column(name = "title_id")
    private String id;

    @Column(name = "name")
    private String title;

    @Column(name = "linked_id")
    private String linkedId;

    public Title() {
    }

    public Title(String title) {
        this.title = title;
    }

    public Title(String title, String linkedId) {
        this.title = title;
        this.linkedId = linkedId;
    }

    public Title(String id, String title, String linkedId) {
        this.id = id;
        this.title = title;
        this.linkedId = linkedId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLinkedId() {
        return linkedId;
    }

    public void setLinkedId(String linkedId) {
        this.linkedId = linkedId;
    }

    @PrePersist
    public void prePersist() {
        if(this.id == null) {
            this.id = Utility.getID();
        }
    }

    @Override
    public String toString() {
        return "Title{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

}
