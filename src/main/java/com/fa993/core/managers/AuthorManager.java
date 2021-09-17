package com.fa993.core.managers;

import com.fa993.core.pojos.Author;
import com.fa993.core.pojos.Title;
import com.fa993.core.repositories.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthorManager {


    AuthorRepository repo;

    private Map<String, Author> authors;

    public AuthorManager(AuthorRepository repo) {
        this.repo = repo;
        this.authors = new HashMap<>();
        repo.findAll().forEach(t -> authors.putIfAbsent(t.getName(), t));
    }

    public Author getAuthor(String author){
        Author a = authors.get(author);
        if(a == null) {
            a = new Author(author);
            a = repo.save(a);
            authors.put(author, a);
        }
        return a;
    }
}
