package com.fa993.core.managers;

import com.fa993.core.dto.PageURL;
import com.fa993.core.repositories.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class PageManager {

    @Autowired
    PageRepository repo;

    public List<PageURL> getByChapterId(String id){
        return repo.findAllByChapterIdOrderByPageNumberAsc(id);
    }

}
