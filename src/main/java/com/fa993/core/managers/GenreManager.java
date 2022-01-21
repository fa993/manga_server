package com.fa993.core.managers;

import com.fa993.core.dto.GenreData;
import com.fa993.core.pojos.Genre;
import com.fa993.core.repositories.GenreRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class GenreManager {

    GenreRepository repo;

    private Map<String, Genre> genres;

    private List<GenreData> data;

    private Genre error;

    private static class GenreDataImpl implements GenreData{

        private String id;

        private String name;

        public GenreDataImpl(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public GenreManager(GenreRepository repo) {
        this.repo = repo;
        this.genres = new HashMap<>();
        this.data = new ArrayList<>();
        this.repo.findAll().forEach(t -> {
            genres.put(t.getName().toLowerCase(), t);
            data.add(new GenreDataImpl(t.getId(), t.getName()));
        });
        this.data.sort(Comparator.comparing(GenreData::getName));
        this.error = this.repo.findByName("not available");
    }

    public Genre getGenre(String genreName) {
        if (genreName == null) {
            return error;
        }
        return genres.getOrDefault(genreName.toLowerCase(), error);
    }

    public void addAll(List<String> toAdd) {
        toAdd.forEach(this::add);
    }

    public void add(String toAdd) {
        String lt = toAdd.toLowerCase();
        Optional.ofNullable(repo.findByName(lt)).ifPresentOrElse(f -> {
        }, () -> {
            genres.put(lt, repo.save(new Genre(lt)));
        });
    }

    public List<GenreData> all() {
        return this.data;
    }

    public void registerError(String error) {
        String lt = error.toLowerCase();
        Optional.ofNullable(repo.findByName(lt)).ifPresentOrElse(f -> this.error = f, () -> this.error = this.repo.save(new Genre(lt)));
    }
}
