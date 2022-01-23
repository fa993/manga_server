package com.fa993.networking.controllers;

import com.fa993.core.dto.*;
import com.fa993.core.exceptions.NoSuchMangaException;
import com.fa993.core.managers.GenreManager;
import com.fa993.core.managers.MangaListingManager;
import com.fa993.core.managers.PageManager;
import com.fa993.core.pojos.*;
import com.fa993.core.managers.MangaManager;
import com.fa993.retrieval.MultiThreadScrapper;
import com.fa993.retrieval.SourceScrapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/public/manga")
public class MangaController {

    public MangaManager mangaManager;
    public PageManager pageManager;
    public GenreManager genreManager;
    public MangaListingManager listingManager;

    public MultiThreadScrapper sct;

    private Map<String, Long> lastWatchesById;

    public MangaController(MangaManager repo1, PageManager repo2, GenreManager repo3, MangaListingManager repo4, MultiThreadScrapper sc) {
        this.mangaManager = repo1;
        this.pageManager = repo2;
        this.genreManager = repo3;
        this.listingManager = repo4;
        this.sct = sc;
        this.lastWatchesById = new HashMap<>();
    }

    @GetMapping("/{id}")
    public CompleteManga getManga(@PathVariable(name = "id") String id) {
        CompleteManga m = this.mangaManager.getById(id);
        Long ref = System.currentTimeMillis();
        WatchData dt = this.mangaManager.getUrlById(m.main().getId());
        if(dt.getLastWatchTime() == null || this.mangaManager.isOld(ref, Math.max(this.lastWatchesById.getOrDefault(m.main().getId(), 0L), dt.getLastWatchTime()))) {
            this.sct.watchSingle(dt.getUrl(), m.main().getSource().getId());
            this.lastWatchesById.put(m.main().getId(), ref);
        }
        for(LinkedMangaData ld : m.related()) {
            WatchData dt0 = this.mangaManager.getUrlById(ld.getId());
            if(dt0.getLastWatchTime() == null || this.mangaManager.isOld(ref, Math.max(this.lastWatchesById.getOrDefault(ld.getId(), 0L), dt0.getLastWatchTime()))) {
                this.sct.watchSingle(dt0.getUrl(), ld.getSource().getId());
                this.lastWatchesById.put(ld.getId(), ref);
            }
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
