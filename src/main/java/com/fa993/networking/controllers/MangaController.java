package com.fa993.networking.controllers;

import com.fa993.core.dto.*;
import com.fa993.core.exceptions.NoSuchMangaException;
import com.fa993.core.managers.GenreManager;
import com.fa993.core.managers.MangaListingManager;
import com.fa993.core.managers.PageManager;
import com.fa993.core.pojos.Manga;
import com.fa993.core.pojos.MangaQuery;
import com.fa993.core.managers.MangaManager;
import com.fa993.core.pojos.MangaQueryResponse;
import com.fa993.retrieval.MultiThreadScrapper;
import com.fa993.retrieval.SourceScrapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/manga")
public class MangaController {

    public MangaManager mangaManager;
    public PageManager pageManager;
    public GenreManager genreManager;
    public MangaListingManager listingManager;

    public MultiThreadScrapper sct;

    public MangaController(MangaManager repo1, PageManager repo2, GenreManager repo3, MangaListingManager repo4, MultiThreadScrapper sc) {
        this.mangaManager = repo1;
        this.pageManager = repo2;
        this.genreManager = repo3;
        this.listingManager = repo4;
        this.sct = sc;
    }

    @GetMapping("/{id}")
    public CompleteManga getManga(@PathVariable(name = "id") String id) {
        CompleteManga m = this.mangaManager.getById(id);
        this.sct.watchSingle(this.mangaManager.getUrlById(m.main().getId()).getUrl(), m.main().getSource().getId());
        for(LinkedMangaData ld : m.related()) {
            this.sct.watchSingle(this.mangaManager.getUrlById(ld.getId()).getUrl(), ld.getSource().getId());
        }
        return m;
    }

    @GetMapping("/part/{id}")
    public LinkedMangaData getPart(@PathVariable(name = "id") String id) {
        return this.mangaManager.getPartById(id);
    }

    @GetMapping("/genres")
    public List<GenreData> getAllGenres() {
        return this.genreManager.all();
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
        return new MangaQueryResponse(query, listingManager.findAllByQuery(query));
    }

    @PostMapping("/home")
    public MangaQueryResponse getHomePage(@RequestBody MangaQuery query) {
        return new MangaQueryResponse(query, listingManager.getHome(query));
    }

    @ExceptionHandler(NoSuchMangaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String invalidId(NoSuchMangaException ex) {
        return ex.getResponseBody();
    }

}
