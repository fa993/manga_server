package com.fa993.core.repositories;

import com.fa993.core.pojos.ProblemChild;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemChildRepository extends JpaRepository<ProblemChild, Integer> {

    public boolean existsByUrl(String url);

}
