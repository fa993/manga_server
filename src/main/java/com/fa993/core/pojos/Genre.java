package com.fa993.core.pojos;

import com.fa993.utils.Utility;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;

@Entity
@Table(name = "genre")
public class Genre {

    @Id
    @Column(name = "genre_id")
    private String id;

    @Column(name = "name")
    @JsonView(Views.Heading.class)
    private String name;

    public Genre() {
    }

    public Genre(String genre) {
        this.name = genre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @PrePersist
    public void prePersist() {
        if(this.id == null) {
            this.id = Utility.getID();
        }
    }

    @Override
    public String toString() {
        return "Genre{" +
                "id='" + id + '\'' +
                ", genre='" + name + '\'' +
                '}';
    }
}
