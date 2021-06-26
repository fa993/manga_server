package com.fa993.core.pojos;

import javax.persistence.*;

@Entity
@Table(name = "problem_child")
public class ProblemChild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_child_id")
    private int id;

    @Column(name = "url")
    private String url;

    public ProblemChild() {
    }

    public ProblemChild(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
