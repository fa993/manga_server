package com.fa993.core.pojos;

import com.fa993.utils.Utility;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "source")
public class Source {

    @Id
    @Column(name = "source_id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "priority")
    private Integer priority;

    public Source() {
    }

    public Source(String id, String name, int priority) {
        this.id = id;
        this.name = name;
        this.priority = priority;
    }

    public Source(String name, int priority){
        this(null, name, priority);
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @PrePersist
    public void prePersist() {
        if(this.id == null) {
            this.id = Utility.getID();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Source source = (Source) o;
        return getId().equals(source.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Source{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
