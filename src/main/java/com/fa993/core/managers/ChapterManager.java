package com.fa993.core.managers;

import com.fa993.core.repositories.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ChapterManager {

    @Autowired
    ChapterRepository repo;

    public void deleteChaps(List<String> ids){
        this.repo.deleteByIdIn(ids);
    }

}
