package com.fa993.retrieval.sources;

import com.fa993.core.exceptions.MangaFetchingException;
import com.fa993.core.exceptions.PageProcessingException;
import com.fa993.core.managers.SourceManager;
import com.fa993.core.pojos.Source;
import com.fa993.retrieval.Scrapper;
import com.fa993.retrieval.SourceScrapper;
import com.fa993.retrieval.pojos.ChapterDTO;
import com.fa993.retrieval.pojos.MangaDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.function.Consumer;

//@Scrapper
public class MangaHasu implements SourceScrapper {

    private static final String WEBSITE_HOST = "https://mangahasu.se";

    private static final String SEARCH_ALL = WEBSITE_HOST + "/directory.html";
    private static final String ALL_GENRE = WEBSITE_HOST + "/";

    private static final String WATCH = WEBSITE_HOST + "/latest-releases.html";

    private static Integer TOTAL_PAGES = 27;

    private static final int NO_OF_PAGES_TO_WATCH = 10;

    private static final DateTimeFormatter DTF = new DateTimeFormatterBuilder().appendPattern("MMM dd, yyyy")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0).toFormatter().withZone(ZoneId.systemDefault());

    private final Source s;

    private static final Set<String> EMPTY_SET = new HashSet<>();

    public MangaHasu(SourceManager manager) {
        this.s = manager.getSource("mangahasu", 3);
    }

    @Override
    public MangaDTO getManga(String url) throws MangaFetchingException {
        Exception e = null;
        boolean error = false;
        MangaDTO mndt = new MangaDTO();
        mndt.setURL(url);
        mndt.setDescription("");
        mndt.setSource(s);
        try {
            Document doc = Jsoup.connect(url).timeout(0).get();
            Optional.ofNullable(doc.selectFirst("div.info-title > h1")).map(t -> t.text().strip()).ifPresentOrElse(t -> {
                mndt.setPrimaryTitle(t);
                mndt.getTitles().add(t);
            }, () -> {
                throw new RuntimeException("No Title Found");
            });
            Optional.ofNullable(doc.selectFirst("div.info-img > img"))
                    .ifPresentOrElse(t -> mndt.setCoverURL(t.attr("abs:src")), () -> {
                        throw new RuntimeException("No Cover Url Found");
                    });
            Optional.ofNullable(doc.selectFirst("div.info-title > h3"))
                    .ifPresent(t -> extractWithTrim(t.text(), mndt.getTitles(), EMPTY_SET, ',', ';'));
            Optional.ofNullable(doc.selectFirst("div.content-info p")).ifPresent(t -> {
                mndt.setDescription(mndt.getDescription() + t.text());
                mndt.setDescription(mndt.getDescription().strip());
            });
            Optional.ofNullable(doc.selectFirst("div.box-des")).ifPresentOrElse(t -> {
                    Elements ele = t.getElementsByClass("info");
                    if(ele.size() > 0) {
                        mndt.getAuthors().add(ele.get(0).text().strip());
                    }
                    if(ele.size() > 1) {
                        mndt.getArtists().add(ele.get(1).text().strip());
                    }
                    if(ele.size() > 3) {
                        ele.get(3).getElementsByTag("a").eachText().stream().map(String::strip).forEach(mndt.getGenres()::add);
                    }
                    if(ele.size() > 4) {
                        mndt.setStatus(ele.get(4).text().strip());
                    }
                },
                    () -> mndt.setStatus("Not Available"));
            Elements ls = doc.select("table.table > tbody > tr");
            for (int i = 0; i < ls.size(); i++) {
                try {
                    mndt.getChapters().add(getChapter(ls.size() - i - 1, ls.get(i).selectFirst("a").attr("abs:href"), ls.get(i).selectFirst("td.date-updated").text().strip()));
                } catch (Exception ex) {
                    e = ex;
                    error = true;
                }
            }
        } catch (Exception ex) {
            System.out.println("Happened Here: " + mndt);
            throw new MangaFetchingException(url, null, ex);
        }
        if (error) {
            throw new MangaFetchingException(url, mndt, e);
        }
        return mndt;
    }

    public ChapterDTO getChapter(Integer sequenceNumber, String url, String updatedAt) throws Exception {
        ChapterDTO cdto = new ChapterDTO();
        cdto.setChapterName("");
        cdto.setChapterNumber(Integer.toString(sequenceNumber + 1));
        cdto.setSequenceNumber(sequenceNumber);
        cdto.setUpdatedAt(DTF.parse(updatedAt, Instant::from));
        Document doc = Jsoup.connect(url).timeout(0).get();
        Optional.ofNullable(doc.selectFirst("span[itemprop]"))
                .ifPresent(t -> cdto.setChapterName(t.text().strip()));
        doc.select("div.img > img").forEach(t -> cdto.getImagesURL().add(t.attr("abs:src")));
        return cdto;
    }

    @Override
    public void reloadCompletePages() {
        //DO NOTHING
        try {
            Document doc = Jsoup.connect(SEARCH_ALL).timeout(0).get();
            String st = doc.selectFirst("a[title=\"Trang cuối\"]").attr("href");
            TOTAL_PAGES = Integer.valueOf(st.substring(st.lastIndexOf('=') + 1));
        } catch (IOException e) {
            System.out.println("Could not get page count");
            e.printStackTrace();
            TOTAL_PAGES = 0;
        }

    }

    @Override
    public Integer getCompleteNumberOfPages() {
        return TOTAL_PAGES;
    }

    @Override
    public List<String> getLiterallyEveryLink(int x) throws PageProcessingException {
        try {
            Document doc = Jsoup.connect(SEARCH_ALL + "?page=" + x).timeout(0).get();
            return doc.select("ul.list_manga div.wrapper_imgage > a").stream().map(t -> t.attr("abs:href")).toList();
        } catch (IOException e) {
            throw new PageProcessingException(x, this.getSource(), e);
        }
    }

    @Override
    public void reloadWatchPages() {
        //DO NOTHING
    }

    @Override
    public Integer getNumberOfPagesToWatch() {
        return NO_OF_PAGES_TO_WATCH;
    }

    @Override
    public List<String> watch(int x) throws PageProcessingException {
        try{
            Document doc = Jsoup.connect(WATCH + "?page=" + x).timeout(0).get();
            return doc.select("ul.list_manga div.wrapper_imgage > a").stream().map(t -> t.attr("abs:href")).toList();
        }catch (IOException ex){
            throw new PageProcessingException(x, this.getSource(), ex);
        }
    }

    @Override
    public Source getSource() {
        return this.s;
    }

    @Override
    public List<String> getAllGenre() {
        final List<String> ls = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(ALL_GENRE).timeout(0).get();
            doc.select("ul.dropdown-menu")
                    .forEach(t -> t.getElementsByTag("li").stream().map(f -> f.text().strip()).forEach(p -> {
                        ls.add(p);
                    }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ls;
    }

}
