package com.fa993.core.managers;

import com.fa993.core.pojos.MangaListing;
import com.fa993.core.repositories.MangaListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class MangaListingManager {

    @Autowired
    MangaListingRepository repo;

    public MangaListing getByMangaId(String mangaId) {
        return repo.getByMangaId(mangaId).orElseGet(() -> new MangaListing(mangaId));
    }
}
