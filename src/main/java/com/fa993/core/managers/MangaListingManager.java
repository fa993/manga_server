package com.fa993.core.managers;

import com.fa993.core.dto.MangaHeading;
import com.fa993.core.pojos.MangaListing;
import com.fa993.core.pojos.MangaQuery;
import com.fa993.core.repositories.MangaListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class MangaListingManager {

    @Autowired
    MangaListingRepository repo;

    @PersistenceContext
    EntityManager manager;

    public MangaListing getByMangaId(String mangaId) {
        return repo.getByMangaId(mangaId).orElseGet(() -> new MangaListing(mangaId));
    }

    public List<MangaHeading> findAllByQuery(MangaQuery query) {
        return (query.getPreferredSourceId() == null ? repo.findIdsWithoutSource("%" + query.getName() + "%", query.getOffset(), query.getLimit()) : repo.findIdsWithSource("%" + query.getName() + "%", query.getPreferredSourceId(), query.getOffset(), query.getLimit()));
    }

    public List<MangaHeading> getHome(MangaQuery query) {
        return (query.getGenreIds() == null || query.getGenreIds().isEmpty() ? repo.getHomePage(query.getOffset(), query.getLimit()) : repo.getHomePage(query.getGenreIds(), query.getGenreIds().size(), query.getOffset(), query.getLimit()));
    }

    public MangaListing save(MangaListing ls) {
        MangaListing l1 = repo.saveAndFlush(ls);
        detachManagedObjects();
        return l1;
    }

    private void detachManagedObjects() {
        manager.clear();
        manager.getEntityManagerFactory().getCache().evictAll();
    }
}
