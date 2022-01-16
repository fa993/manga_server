package com.fa993.core.managers;

import com.fa993.core.dto.*;
import com.fa993.core.exceptions.NoSuchMangaException;
import com.fa993.core.pojos.Manga;
import com.fa993.core.pojos.MangaListing;
import com.fa993.core.pojos.MangaQuery;
import com.fa993.core.pojos.Source;
import com.fa993.core.repositories.MangaRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Priority;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

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


    MangaRepository repo;

    MangaListingManager listingManager;

    @PersistenceContext
    EntityManager manager;

    private Map<String, Set<Integer>> priorities;

    public MangaManager(MangaRepository repo, MangaListingManager manager1) {
        this.repo = repo;
        this.listingManager = manager1;
        this.priorities = new HashMap<>();
        this.repo.getAllBy().forEach(t -> {
            this.priorities.putIfAbsent(t.getLinkedId(), new HashSet<>());
            this.priorities.get(t.getLinkedId()).add(t.getSource().getPriority());
        });
    }

    public CompleteManga getById(String id) {
        MainMangaData main = Optional.ofNullable(repo.getById(id)).orElseThrow(() -> NoSuchMangaException.constructFromID(id));
        List<LinkedMangaData> linked = repo.findAllByLinkedIdAndIdNot(main.getLinkedId(), main.getId());
        return new CompleteManga(main, linked);
    }

    public LinkedMangaData getPartById(String id) {
        return repo.readById(id);
    }

    public boolean isAlreadyProcessed(String url) {
        return repo.existsByUrl(url);
    }

    public String getId(String url) {
        MangaID id = repo.findByUrl(url);
        return id == null ? null : id.getId();
    }

    public Boolean isPrimary(String linkedId, Integer priority) {
        this.priorities.putIfAbsent(linkedId, new HashSet<>());
        Set<Integer> mps = this.priorities.get(linkedId);
        if (mps.isEmpty()) {
            mps.add(priority);
            return true;
        } else {
            Boolean ret = mps.stream().reduce(true, (r, e) -> {
                if (r == null) {
                    return r;
                }
                int x = priority - e;
                if (x == 0) {
                    return null;
                }
                return x < 0 && r;
            }, (l, r) -> {
                if (l == null || r == null) {
                    return null;
                } else {
                    return l && r;
                }
            });
            mps.add(priority);
            return ret;
        }
    }

    public Manga insert(boolean firstTime, Manga manga) {
        if(repo.existsByUrl(manga.getUrl())) {
            repo.deleteByUrl(manga.getUrl());
        }
        if (firstTime) {
            Boolean ret = isPrimary(manga.getLinkedId(), manga.getSource().getPriority());
            manga.setMain(ret);
            if (ret != null && ret) {
                //TODO sometimes deadlock occurs here
                repo.updateMainState(manga.getLinkedId());
            }
        }
        Manga m = repo.saveAndFlush(manga);
        MangaListing ls = listingManager.getByMangaId(m.getId());
        ls.setName(m.getName());
        ls.setCoverURL(m.getCoverURL());
        ls.setDescriptionSmall(m.getDescription().substring(0, Math.min(m.getDescription().length(), 255)));
        StringBuilder sb1 = m.getGenres().stream().collect(StringBuilder::new, (sb, g) -> sb.append(StringUtils.capitalize(g.getName())).append(','), StringBuilder::append);
        sb1.deleteCharAt(sb1.length() - 1);
        ls.setGenres(sb1.toString());
        listingManager.save(ls);
        detachManagedObjects();
        return m;
    }

    public void detachManagedObjects() {
        manager.clear();
        manager.getEntityManagerFactory().getCache().evictAll();
    }

    public void deleteAll() {
    }
}
