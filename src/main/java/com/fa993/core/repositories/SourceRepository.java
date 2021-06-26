package com.fa993.core.repositories;

import com.fa993.core.pojos.Source;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRepository extends JpaRepository<Source, String> {

    public Source findByName(String name);

}
