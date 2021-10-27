package com.fa993.core.repositories;

import com.fa993.core.pojos.MangaListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MangaListingRepository extends JpaRepository<MangaListing, Integer> {

    public Optional<MangaListing> getByMangaId(String mangaId);

}
