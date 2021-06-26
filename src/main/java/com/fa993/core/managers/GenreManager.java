package com.fa993.core.managers;

import com.fa993.core.pojos.Genre;
import com.fa993.core.repositories.GenreRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GenreManager {

    GenreRepository repo;

    private Map<String, Genre> genres;

    private Genre error;

    public GenreManager(GenreRepository repo) {
        this.repo = repo;
        this.genres = new HashMap<>();
        this.repo.findAll().forEach(t -> genres.put(t.getName().toLowerCase(), t));
        this.error = this.repo.findByName("not available");
    }

    public Genre getGenre(String genreName) {
        if (genreName == null) {
            return error;
        }
        return genres.getOrDefault(genreName.toLowerCase(), error);
    }

    public void addAll(List<String> toAdd) {
        toAdd.forEach(t -> add(t));
    }

    public void add(String toAdd) {
        String lt = toAdd.toLowerCase();
        Optional.ofNullable(repo.findByName(lt)).ifPresentOrElse(f -> {
        }, () -> {
            genres.put(lt, repo.save(new Genre(lt)));
        });
    }

    public void registerError(String error){
        String lt = error.toLowerCase();
        Optional.ofNullable(repo.findByName(lt)).ifPresentOrElse(f -> this.error = f, () -> this.error = this.repo.save(new Genre(lt)));
    }
}
