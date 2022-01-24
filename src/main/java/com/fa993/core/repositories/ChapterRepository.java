package com.fa993.core.repositories;

import com.fa993.core.pojos.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ChapterRepository extends JpaRepository<Chapter, String> {

    public void deleteByIdIn(Collection<String> ids);

}
