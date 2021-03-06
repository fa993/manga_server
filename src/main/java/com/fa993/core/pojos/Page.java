package com.fa993.core.pojos;

import com.fa993.core.dto.PageURL;
import com.fa993.core.dto.PageURLImpl;
import org.hibernate.annotations.ColumnTransformer;

import javax.persistence.*;

@Entity
@Table(name = "chapter_page")
public class Page {

    @Id
    @Column(name = "chapter_page_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "chapter_id")
    private String chapterId;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "url")
//    @ColumnTransformer(forColumn = "url", read = "UNCOMPRESS(url)", write = "COMPRESS(?)")
    private String url;

    public Page() {
    }


    public Page(String url) {
        this.url = url;
    }

    public Page(Integer pageNumber, String url) {
        this.pageNumber = pageNumber;
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PageURL toPageURL() {
        return new PageURLImpl(this.url);
    }

    @Override
    public String toString() {
        return "Page{" +
                "id=" + id +
                ", url='" + url + '\'' +
                '}';
    }
}
