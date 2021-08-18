package com.fa993.core.managers;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fa993.core.dto.PageURL;
import com.fa993.core.repositories.PageRepository;

@Service
@Transactional
public class PageManager {

    private static final String COUNT_PAGES_BEFORE_CHAPTER = "SELECT COUNT(chapter_page_id) FROM chapter_page where exists (SELECT chapter_id FROM chapter WHERE chapter_page.chapter_id = chapter.chapter_id AND manga_id = :query1 AND sequence_number < :query2 )";

    private static final String MANGA_ID_PARAM = "query1";
    private static final String SEQUENCE_NUMBER_PARAM = "query2";

    @Autowired
    PageRepository repo;

    @PersistenceContext
    EntityManager manager;

    public List<PageURL> getByChapterId(String id) {
        return repo.findAllByChapterIdOrderByPageNumberAsc(id);
    }

    public BigInteger getTotalPageNumber(String mangaId, Integer sequenceNumber) {
        return (BigInteger) manager.createNativeQuery(COUNT_PAGES_BEFORE_CHAPTER).setParameter(MANGA_ID_PARAM, mangaId).setParameter(SEQUENCE_NUMBER_PARAM, sequenceNumber).getSingleResult();
    }

}
