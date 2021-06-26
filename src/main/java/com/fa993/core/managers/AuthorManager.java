package com.fa993.core.managers;

import com.fa993.core.pojos.Author;
import com.fa993.core.repositories.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthorManager {

    @Autowired
    AuthorRepository repo;

    public Author getAuthor(String author){
        return Optional.ofNullable(repo.findByName(author)).orElseGet(() -> repo.save(new Author(author)));
    }
}
