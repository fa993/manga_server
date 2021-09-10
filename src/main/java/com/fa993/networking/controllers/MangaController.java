package com.fa993.networking.controllers;

import com.fa993.core.dto.ChapterPosition;
import com.fa993.core.dto.CompleteManga;
import com.fa993.core.dto.MangaHeadingProper;
import com.fa993.core.dto.PageURL;
import com.fa993.core.exceptions.NoSuchMangaException;
import com.fa993.core.managers.PageManager;
import com.fa993.core.pojos.MangaQuery;
import com.fa993.core.pojos.MangaQueryResponse;
import com.fa993.core.managers.MangaManager;
import com.fa993.core.pojos.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/public/manga")
public class MangaController {

    public MangaManager mangaManager;
    public PageManager pageManager;

    public MangaController(MangaManager repo1, PageManager repo2) {
        this.mangaManager = repo1;
        this.pageManager = repo2;
    }

    @GetMapping("/{id}")
    public CompleteManga getManga(@PathVariable(name = "id") String id) {
        return this.mangaManager.getById(id);
    }

    @GetMapping("/chapter/{id}")
    public List<PageURL> getChapter(@PathVariable(name = "id") String id) {
        return this.pageManager.getByChapterId(id);
    }

    @GetMapping("/chapter/position/{manga_id}/{sequence_number}")
    public ChapterPosition getChapterIndex(@PathVariable(name = "manga_id") String mangaId, @PathVariable(name = "sequence_number") Integer sequenceNumber) {
        return this.pageManager.getPosition(mangaId, sequenceNumber);
    }

    @PostMapping("/search")
    public MangaQueryResponse getManga(@RequestBody MangaQuery query) {
        return new MangaQueryResponse(query, mangaManager.findAllByQuery(query));
    }

    @PostMapping("/home")
    public MangaQueryResponse getHomePage(@RequestBody MangaQuery query) {
        return new MangaQueryResponse(query, mangaManager.getHome(query));
    }

    @ExceptionHandler(NoSuchMangaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String invalidId(NoSuchMangaException ex) {
        return ex.getResponseBody();
    }

}
