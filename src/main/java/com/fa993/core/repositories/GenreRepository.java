package com.fa993.core.repositories;

import com.fa993.core.pojos.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, String> {

    public Genre findByName(String genre);

}
