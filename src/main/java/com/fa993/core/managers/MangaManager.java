package com.fa993.core.managers;

import com.fa993.core.dto.*;
import com.fa993.core.exceptions.NoSuchMangaException;
import com.fa993.core.pojos.Manga;
import com.fa993.core.pojos.MangaQuery;
import com.fa993.core.repositories.MangaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MangaManager {

    private static final String FIND_QUERY_WITH_SOURCE_V2 = "select manga.manga_id from manga where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.title LIKE :query1 ) AND manga.source_id = :query2 UNION select manga.manga_id from manga where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.title LIKE :query1 ) AND manga.is_main = 1";

    private static final String FIND_QUERY_WITHOUT_SOURCE_V2 = "select manga.manga_id from manga where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.title LIKE :query1 ) AND manga.is_main = 1";

    private static final String HOME_PAGE_QUERY_V2 = "select manga.manga_id from manga, manga_genre where manga.manga_id = manga_genre.manga_id" +
//            " and manga_genre.genre_id in (:query4)" +
            " and manga.is_main order by manga.name ASC";

    private static final String FETCH_HEADING_V2 = "select manga.manga_id, manga.name, manga.cover_url, manga.description_small, group_concat(genre.name separator ', ') as genres from manga, manga_genre, genre where manga_genre.genre_id = genre.genre_id AND manga_genre.manga_id = manga.manga_id AND manga.manga_id = :query3 AND manga.is_main";

    private static final String NAME_PARAM = "query1";
    private static final String SOURCE_PARAM = "query2";
    private static final String ID_PARAM = "query3";
    private static final String GENRE_PARAM = "query4";

    @Autowired
    MangaRepository repo;

    @PersistenceContext
    EntityManager manager;

    public MangaManager() {
    }

    public List<MangaHeading> findAllByQuery(MangaQuery query) {
        return (query.getPreferredSourceId() == null ? repo.findIdsWithoutSource("%" + query.getName() + "%", query.getOffset(), query.getLimit()) : repo.findIdsWithSource("%" + query.getName() + "%", query.getPreferredSourceId(), query.getOffset(), query.getLimit())).stream().map(t -> repo.fetchHeading(t)).toList();
    }

    public List<MangaHeading> getHome(MangaQuery query) {
        return repo.getHomePage(query.getOffset(), query.getLimit()).stream().map(t -> repo.fetchHeading(t)).toList();
    }

    public CompleteManga getById(String id) {
        MainMangaData main = Optional.ofNullable(repo.getById(id)).orElseThrow(() -> NoSuchMangaException.constructFromID(id));
        List<LinkedMangaData> linked = repo.findAllByLinkedIdAndIdNot(main.getLinkedId(), main.getId());
        return new CompleteManga(main, linked);
    }

    public boolean isAlreadyProcessed(String url) {
        return repo.existsByUrl(url);
    }

    public Manga getMangaByURL(String url) {
        return repo.findByUrl(url);
    }


    public void insert(Manga manga) {
        List<MangaPriority> pri = repo.findAllByLinkedId(manga.getLinkedId());
        boolean ret = pri.stream().reduce(true, (r, e) -> manga.getSource().getPriority() - e.getSource().getPriority() < 0 && r, (l, r) -> l && r);
        manga.setMain(ret);
        if (ret) {
            //TODO sometimes deadlock occurs here
            repo.updateMainState(manga.getLinkedId());
        }
        repo.save(manga);
    }

    public void update(Manga manga) {
        manager.merge(manga);
    }

    public void deleteAll() {
    }
}
