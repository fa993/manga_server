package com.fa993.core.repositories;

import com.fa993.core.dto.LinkedMangaData;
import com.fa993.core.dto.MainMangaData;
import com.fa993.core.dto.MangaHeadingProper;
import com.fa993.core.pojos.Manga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MangaRepository extends JpaRepository<Manga, String> {

    public boolean existsByUrl(String url);
    public Page<MangaHeadingProper> findAllBy(Pageable pageable);
    public List<LinkedMangaData> findAllByLinkedIdAndIdNot(String linkedId, String mangaId);
    public MainMangaData getById(String id);
    public Manga findByUrl(String url);
}
