package com.fa993.core.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fa993.core.pojos.ProblemChild;
import com.fa993.core.repositories.ProblemChildRepository;

@Service
public class ProblemChildManager {

    @Autowired
    ProblemChildRepository repo;

    public void insert(String x) {
        if(!repo.existsByUrl(x)){
            repo.save(new ProblemChild(x));
        }
    }

    public boolean isProblem(String url) {
        return repo.existsByUrl(url);
    }

}
