package com.fa993.core.repositories;

import com.fa993.core.pojos.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, String> {

    public Author findByName(String name);
}
