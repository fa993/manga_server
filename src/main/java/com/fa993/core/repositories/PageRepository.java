package com.fa993.core.repositories;

import com.fa993.core.dto.PageURL;
import com.fa993.core.pojos.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

public interface PageRepository extends JpaRepository<Page, Integer> {

    public List<PageURL> findAllByChapterIdOrderByPageNumberAsc(String chapterId);

    @Query(
            value = "SELECT COUNT(chapter_page_id) FROM chapter_page WHERE exists (SELECT chapter_id FROM chapter WHERE chapter_page.chapter_id = chapter.chapter_id AND manga_id = :query1 AND sequence_number < :query2 )",
            nativeQuery = true
    )
    public BigInteger getPositionOfPage(@Param(value = "query1") String mangaId, @Param(value = "query2") Integer sequenceNumber);

    @Query(
            value = "SELECT COUNT(chapter_page_id) FROM chapter_page WHERE exists (SELECT chapter_id FROM chapter WHERE chapter_page.chapter_id = chapter.chapter_id AND manga_id = :query1 )",
            nativeQuery = true
    )
    public BigInteger getCountOfPages(@Param(value = "query1") String mangaId);

}
