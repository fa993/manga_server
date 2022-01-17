package com.fa993.core.repositories;

import com.fa993.core.dto.MangaHeading;
import com.fa993.core.pojos.MangaListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MangaListingRepository extends JpaRepository<MangaListing, Integer> {

    public long deleteByMangaId(String mangaId);

    public Optional<MangaListing> getByMangaId(String mangaId);

    @Query(
            value = "select manga_listing.manga_id as id, manga_listing.name as name, manga_listing.cover_url as coverURL, manga_listing.description_small as smallDescription, manga_listing.genres as genres from manga, manga_listing where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.title LIKE :query1 ) AND manga.is_main = 1 AND manga_listing.manga_id = manga.manga_id limit :offset, :limit",
            nativeQuery = true
    )
    public List<MangaHeading> findIdsWithoutSource(@Param(value = "query1") String searchString, @Param(value = "offset") int offset, @Param(value = "limit") int limit);

    @Query(
            value = "select manga_listing.manga_id as id, manga_listing.name as name, manga_listing.cover_url as coverURL, manga_listing.description_small as smallDescription, manga_listing.genres as genres from manga, manga_listing where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.title LIKE :query1 ) AND manga.source_id = :query2 AND manga.is_main IS NOT NULL AND manga_listing.manga_id = manga.manga_id UNION select manga_listing.manga_id as id, manga_listing.name as name, manga_listing.cover_url as coverURL, manga_listing.description_small as smallDescription, manga_listing.genres as genres from manga, manga_listing where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.title LIKE :query1 ) AND manga.is_main = 1 AND manga_listing.manga_id = manga.manga_id limit :offset, :limit",
            nativeQuery = true
    )
    public List<MangaHeading> findIdsWithSource(@Param(value = "query1") String searchString, @Param(value = "query2") String sourceId, @Param(value = "offset") int offset, @Param(value = "limit") int limit);

    //TODO test a self join query to capture genre in different sources
    @Query(
            value = "select manga_listing.manga_id as id, manga_listing.name as name, manga_listing.cover_url as coverURL, manga_listing.description_small as smallDescription, manga_listing.genres as genres from manga, manga_listing, manga_genre where manga.manga_id = manga_genre.manga_id AND manga.manga_id = manga_listing.manga_id AND manga_genre.genre_id IN :genres AND manga.is_main = 1 group by manga.manga_id HAVING count(*) = :size order by manga.name ASC limit :offset, :limit",
            nativeQuery = true
    )
    public List<MangaHeading> getHomePage(@Param(value = "genres") List<String> genres, @Param(value = "size") int size, @Param(value = "offset") int offset, @Param(value = "limit") int limit);

    //TODO test a self join query to capture genre in different sources
    @Query(
            value = "select manga_listing.manga_id as id, manga_listing.name as name, manga_listing.cover_url as coverURL, manga_listing.description_small as smallDescription, manga_listing.genres as genres from manga, manga_listing where manga.is_main = 1 AND manga.manga_id = manga_listing.manga_id  order by manga.name ASC limit :offset, :limit",
            nativeQuery = true
    )
    public List<MangaHeading> getHomePage(@Param(value = "offset") int offset, @Param(value = "limit") int limit);


}
