package com.fa993.core.managers;

import com.fa993.core.pojos.Source;
import com.fa993.core.pojos.Title;
import com.fa993.core.repositories.TitleRepository;
import com.fa993.utils.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TitleManager {

    @Autowired
    TitleRepository repo;

    @PersistenceContext
    EntityManager manager;

    public Title getTitle(String title) {
        return Optional.ofNullable(repo.findFirstByTitle(title)).orElseGet(() -> repo.save(new Title(title)));
    }

    public String add(List<String> titles, Source c) {
        String ret = null;
        for (String x : titles) {
            ret = Optional.ofNullable(repo.findFirstByTitle(x)).map(t -> t.getLinkedId()).orElse(null);
            if (ret != null) {
                break;
            }
        }
        if (ret == null) {
            ret = Utility.getID();
        }
        String finalRet = ret;
        titles.stream().forEach(t -> manager.persist(new Title(t, finalRet)));
        return ret;
    }

}
