package com.fa993.core.repositories;

import com.fa993.core.dto.PageURL;
import com.fa993.core.pojos.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, Integer> {

    public List<PageURL> findAllByChapterIdOrderByPageNumberAsc(String chapterId);

}
