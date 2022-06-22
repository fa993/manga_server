package com.fa993.core.managers;

import com.fa993.core.pojos.Source;
import com.fa993.core.pojos.SourcePattern;
import com.fa993.core.repositories.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SourceManager {

    @Autowired
    SourceRepository repo;

    public Source getSource(String source, int priority, String[] patterns) {
        return Optional.ofNullable(repo.findByName(source)).orElseGet(() -> repo.save(new Source(source, priority, Arrays.stream(patterns).map(SourcePattern::new).toList())));
    }

    public Source getSource(String id) throws NoSuchElementException {
        return repo.findById(id).get();
    }

    public Map<String, String> getPatterns() {
        return repo.findAll().stream().flatMap(t -> t.getPatterns().stream()).collect(Collectors.toMap(SourcePattern::getUrl, SourcePattern::getSourceId));
    }

    public void saveEntity(Source s) {
        repo.save(s);
    }
}
