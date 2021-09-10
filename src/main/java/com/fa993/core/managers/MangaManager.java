package com.fa993.core.managers;

import com.fa993.core.dto.*;
import com.fa993.core.exceptions.NoSuchMangaException;
import com.fa993.core.pojos.Manga;
import com.fa993.core.pojos.MangaQuery;
import com.fa993.core.repositories.MangaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MangaManager {

    private static final String FIND_QUERY_WITH_SOURCE = "select manga.manga_id from manga where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.name LIKE :query1 ) AND source_id = :query2 group by linked_id UNION ALL select manga.manga_id from manga where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.name LIKE :query1 ) group by linked_id having max(source_id = :query2) = 0";
    private static final String FIND_QUERY_WITHOUT_SOURCE = "select manga.manga_id from manga where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.name LIKE :query1 ) group by linked_id";

    private static final String FETCH_HEADING = "select manga.manga_id, manga.name, manga.cover_url, manga.description_small, group_concat(distinct genre.name separator ', ') as genres from manga, manga_genre, genre where manga_genre.genre_id = genre.genre_id AND manga_genre.manga_id = manga.manga_id AND manga.manga_id = :query3";

    private static final String FIND_QUERY_WITHOUT_SOURCE_NEW = "select manga.linked_id, min(source.priority) as o1 from manga inner join source on source.source_id = manga.source_id where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.name LIKE :query1 ) group by manga.linked_id order by o1 ASC";
    private static final String FIND_QUERY_WITH_SOURCE_NEW = "select manga.linked_id, min(source.priority) as o1 from manga inner join source on source.source_id = manga.source_id where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.name LIKE :query1 ) group by manga.linked_id order by o1 ASC having max(source.source_id = :query2) = 1 UNION select manga.linked_id, min(source.priority) as o1 from manga inner join source on source.source_id = manga.source_id where exists (select linked_id from title where manga.linked_id = title.linked_id AND title.name LIKE :query1 ) group by manga.linked_id order by o1 ASC";

    private static final String FIND_QUERY_BY_GENRE = "select manga.linked_id, min(manga.name) as o1 from manga, manga_genre where manga.manga_id = manga_genre.manga_id" +
//            " and manga_genre.genre_id in (:query4)" +
            " group by manga.linked_id order by o1 ASC";


    //TODO Both the queries are wrong... correct them
    private static final String FETCH_HEADING_NEW = "select manga.manga_id, manga.name, manga.cover_url, manga.description_small, group_concat(distinct genre.name separator ', ') as genres from manga, manga_genre, genre, source where source.source_id = manga.source_id AND manga_genre.genre_id = genre.genre_id AND manga_genre.manga_id = manga.manga_id AND manga.linked_id = :query3 order by source.priority ASC limit 1";

    private static final String FETCH_HEADING_ALPHABET_NEW = "select manga.manga_id, manga.name, manga.cover_url, manga.description_small, group_concat(distinct genre.name separator ', ') as genres from manga, manga_genre, genre where manga_genre.genre_id = genre.genre_id AND manga_genre.manga_id = manga.manga_id AND manga.linked_id = :query3 order by manga.name ASC limit 1";

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

    public Collection<MangaHeading> findAllByQuery(MangaQuery query) {
        List ids;
        if (query.getPreferredSourceId() != null) {
            ids = manager.createNativeQuery(FIND_QUERY_WITH_SOURCE_NEW)
                    .setParameter(NAME_PARAM, "%" + query.getName() + "%")
                    .setParameter(SOURCE_PARAM, query.getPreferredSourceId())
                    .setFirstResult(query.getOffset())
                    .setMaxResults(query.getLimit())
                    .getResultList();
        } else {
            ids = manager.createNativeQuery(FIND_QUERY_WITHOUT_SOURCE_NEW)
                    .setParameter(NAME_PARAM, "%" + query.getName() + "%")
                    .setFirstResult(query.getOffset())
                    .setMaxResults(query.getLimit())
                    .getResultList();
        }
        final Query q0 = manager.createNativeQuery(FETCH_HEADING_NEW);
        return getHeadings(ids, q0);
    }

    public CompleteManga getById(String id) {
        MainMangaData main = Optional.ofNullable(repo.getById(id)).orElseThrow(() -> NoSuchMangaException.constructFromID(id));
        List<LinkedMangaData> linked = repo.findAllByLinkedIdAndIdNot(main.getLinkedId(), main.getId());
        return new CompleteManga(main, linked);
    }

    public Manga junk() {
        return null;
    }

    public boolean isAlreadyProcessed(String url) {
        return repo.existsByUrl(url);
    }
    
    public Manga getMangaByURL(String url) {
    	return repo.findByUrl(url);
    }


    public void insert(Manga manga) {
        manager.persist(manga);
    }
    
    public void update(Manga manga) {
    	manager.merge(manga);
    }
    
    public Collection<MangaHeading> getHome(MangaQuery query){
        List ids = manager.createNativeQuery(FIND_QUERY_BY_GENRE)
//                    .setParameter(GENRE_PARAM, "")
                    .setFirstResult(query.getOffset())
                    .setMaxResults(query.getLimit())
                    .getResultList();
        final Query q0 = manager.createNativeQuery(FETCH_HEADING_ALPHABET_NEW);
        return getHeadings(ids, q0);
    }

    private List getHeadings(List ids, Query q0) {
        @SuppressWarnings("unchecked")
        List hdsN = ids.stream().map(t -> {
            Object[] ta = (Object[]) t;
            Object obj = q0.setParameter(ID_PARAM, ta[0].toString()).getSingleResult();
            Object[] asd = (Object[]) obj;
            return new MangaHeading(asd[0].toString(), asd[1].toString(), asd[2].toString(), asd[3].toString(), asd[4].toString());
        }).toList();
        return hdsN;
    }

    public void deleteAll() {
    }
}
