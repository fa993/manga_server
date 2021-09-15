package com.fa993.core.repositories;

import com.fa993.core.pojos.Title;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TitleRepository extends JpaRepository<Title, String> {

    public Title findFirstByTitleIgnoreCase(String title);

}
