package com.fa993.core.repositories;

import com.fa993.core.dto.*;
import com.fa993.core.pojos.Manga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MangaRepository extends JpaRepository<Manga, String> {

	public boolean existsByUrl(String url);

	public MangaID findByUrl(String url);

	public List<LinkedMangaData> findAllByLinkedIdAndIdNot(String linkedId, String mangaId);

	public MainMangaData getById(String id);

	public LinkedMangaData readById(String id);

	public List<MangaPriority> findAllByLinkedId(String linkedId);

	public List<MangaPriority> getAllBy();

	@Modifying
	@Query(

			value = "update manga set is_main = 0 where linked_id = :link AND is_main = 1",
			nativeQuery = true
	)
	public void updateMainState(@Param(value = "link") String linkedId);

	@Query(
			value = "select manga.manga_id as id, manga.name as name, manga.cover_url as coverURL, manga.description_small as descriptionSmall, group_concat(genre.name separator ', ') as genres from manga, manga_genre, genre where manga_genre.genre_id = genre.genre_id AND manga.manga_id = manga_genre.manga_id AND exists (select linked_id from title where manga.linked_id = title.linked_id AND title.title LIKE :query1 ) AND manga.is_main = 1 group by manga.manga_id limit :offset, :limit",
			nativeQuery = true
	)
	public List<MangaHeading> findQuery(@Param(value = "query1") String searchString, @Param(value = "offset") int offset, @Param(value = "limit") int limit);

	@Query(
			value = "select manga.manga_id from manga where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.title LIKE :query1 ) AND manga.is_main = 1 limit :offset, :limit",
			nativeQuery = true
	)
	public List<String> findIdsWithoutSource(@Param(value = "query1") String searchString, @Param(value = "offset") int offset, @Param(value = "limit") int limit);

	@Query(
			value = "select manga.manga_id from manga where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.title LIKE :query1 ) AND manga.source_id = :query2 AND manga.is_main IS NOT NULL UNION select manga.manga_id from manga where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.title LIKE :query1 ) AND manga.is_main = 1 limit :offset, :limit",
			nativeQuery = true
	)
	public List<String> findIdsWithSource(@Param(value = "query1") String searchString, @Param(value = "query2") String sourceId, @Param(value = "offset") int offset, @Param(value = "limit") int limit);


	@Query(
			value = "select manga.manga_id from manga_genre INNER JOIN manga on manga.manga_id = manga_genre.manga_id where manga_genre.genre_id IN :genres AND manga.is_main = 1 group by manga.manga_id HAVING count(*) = :size order by manga.name ASC limit :offset, :limit",
			nativeQuery = true
	)
	public List<String> getHomePage(@Param(value = "genres") List<String> genres, @Param(value = "size") int size, @Param(value = "offset") int offset, @Param(value = "limit") int limit);

	@Query(
			value = "select manga.manga_id from manga where manga.is_main = 1 order by manga.name ASC limit :offset, :limit",
			nativeQuery = true
	)
	public List<String> getHomePage(@Param(value = "offset") int offset, @Param(value = "limit") int limit);

	@Query(
			value = "select manga.manga_id as id, manga.name as name, manga.cover_url as coverURL, manga.description_small as smallDescription, group_concat( distinct genre.name separator ', ') as genres from manga, manga_genre, genre where manga_genre.genre_id = genre.genre_id AND manga.manga_id = manga_genre.manga_id AND manga.manga_id = :query3 limit 1",
			nativeQuery = true
	)
	public MangaHeading fetchHeading(@Param(value = "query3") String id);
}
