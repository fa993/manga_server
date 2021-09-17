package com.fa993.core.managers;

import com.fa993.core.pojos.Title;
import com.fa993.core.repositories.TitleRepository;
import com.fa993.utils.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TitleManager {

    TitleRepository repo;

    private Map<String, String> allTitles;

    public TitleManager(TitleRepository repo) {
        this.repo = repo;
        this.allTitles = new HashMap<>();
        repo.findAll().forEach(t -> allTitles.put(t.getTitle().toUpperCase(), t.getLinkedId()));
    }

    public void insertTitle(String title, String linkedId) {
        allTitles.computeIfAbsent(title.toUpperCase(), k -> {
            repo.save(new Title(title, linkedId));
            return linkedId;
        });
    }

    public String add(List<String> titles) {
        String ret = null;
        for (String x : titles) {
            ret = this.allTitles.get(x.toUpperCase());
            if (ret != null) {
                break;
            }
        }
        if (ret == null) {
            ret = Utility.getID();
        }
        String finalRet = ret;
        titles.forEach(t -> this.insertTitle(t, finalRet));
        return ret;
    }

}
