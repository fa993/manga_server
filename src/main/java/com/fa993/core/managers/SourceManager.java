package com.fa993.core.managers;

import com.fa993.core.pojos.Source;
import com.fa993.core.repositories.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SourceManager {

    @Autowired
    SourceRepository repo;

    public Source getSource(String source) {
        return Optional.ofNullable(repo.findByName(source)).orElseGet(() -> repo.save(new Source(source)));
    }

}
