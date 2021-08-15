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

    private static final String FIND_QUERY_WITH_SOURCE = "select manga.manga_id from manga where exists (select linked_id from title where manga.name LIKE :query1 ) AND source_id = :query2 group by linked_id UNION ALL select manga.manga_id from manga where exists (select linked_id from title where manga.name LIKE :query1) group by linked_id having max(source_id = :query2) = 0";
    private static final String FIND_QUERY_WITHOUT_SOURCE = "select manga.manga_id from manga where exists (select linked_id from title where manga.name LIKE :query1) group by linked_id";

    private static final String FETCH_HEADING = "select manga.manga_id, manga.name, manga.cover_url, manga.description_small, group_concat(distinct genre.name separator ', ') as genres from manga, manga_genre, genre where manga_genre.genre_id = genre.genre_id AND manga_genre.manga_id = manga.manga_id AND manga.manga_id = :query3";

    private static final String NAME_PARAM = "query1";
    private static final String SOURCE_PARAM = "query2";
    private static final String ID_PARAM = "query3";

    @Autowired
    MangaRepository repo;

    @PersistenceContext
    EntityManager manager;

    public MangaManager() {
    }

    public Collection<MangaHeading> findAllByQuery(MangaQuery query) {
        List ids;
        if (query.getPreferredSourceId() != null) {
            ids = manager.createNativeQuery(FIND_QUERY_WITH_SOURCE)
                    .setParameter(NAME_PARAM, "%" + query.getName() + "%")
                    .setParameter(SOURCE_PARAM, query.getPreferredSourceId())
                    .setFirstResult(query.getOffset())
                    .setMaxResults(query.getLimit())
                    .getResultList();
        } else {
            ids = manager.createNativeQuery(FIND_QUERY_WITHOUT_SOURCE)
                    .setParameter(NAME_PARAM, "%" + query.getName() + "%")
                    .setFirstResult(query.getOffset())
                    .setMaxResults(query.getLimit())
                    .getResultList();
        }
        final Query q0 = manager.createNativeQuery(FETCH_HEADING);
        @SuppressWarnings("unchecked")
        List hdsN = ids.stream().map(t -> {
            Object obj = q0.setParameter(ID_PARAM, t.toString()).getSingleResult();
            Object[] asd = (Object[]) obj;
            return new MangaHeading(asd[0].toString(), asd[1].toString(), asd[2].toString(), asd[3].toString(), asd[4].toString());
        }).toList();
        return hdsN;
    }

    public CompleteManga getById(String id) {
        MainMangaData main = Optional.ofNullable(repo.getById(id)).orElseThrow(() -> new NoSuchMangaException(id));
        List<LinkedMangaData> linked = repo.findAllByLinkedIdAndIdNot(main.getLinkedId(), main.getId());
        return new CompleteManga(main, linked);
    }

    public Manga junk() {
        return null;
    }

    public boolean isAlreadyProcessed(String url) {
        return repo.existsByUrl(url);
    }


    public void insert(Manga manga) {
        manager.persist(manga);
    }

    public MangaHeadingProper tthumbnail() {
        return repo.findAllBy(PageRequest.of(0, 1)).toList().get(0);
    }

    public List<MangaHeadingProper> thome(int offset, int limit) {
//        return ds.find(Manga.class).iterator(new FindOptions().projection().include("name", "url", "coverURL", "id", "metadata.description", "metadata.genres").skip(offset).limit(limit)).toList();
        return repo.findAllBy(PageRequest.of(offset / limit, limit)).toList();
    }

    public HomePageRoot getHomeData() {
        return null;
    }

    public void deleteAll() {
    }
}
