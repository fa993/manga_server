package com.fa993.scrapper.sources;

import com.fa993.core.managers.SourceManager;
import com.fa993.core.pojos.Source;
import com.fa993.retrieval.Scrapper;
import com.fa993.retrieval.SourceScrapper;
import com.fa993.retrieval.pojos.ChapterDTO;
import com.fa993.retrieval.pojos.MangaDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.function.Consumer;

@Scrapper
public class ReadM implements SourceScrapper {

    private static final String WEBSITE_HOST = "https://readm.org";

    private static final String SEARCH_ALL = WEBSITE_HOST + "/manga-list";
    private static final String ALL_GENRE = WEBSITE_HOST + "/advanced-search";
    private static final Integer TOTAL_PAGES = 27;

    private static final DateTimeFormatter DTF = new DateTimeFormatterBuilder()
            .appendPattern("dd MMMM yyyy")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.systemDefault());

    private final Source s;

    private Consumer<String> pFunc;

    public ReadM(SourceManager manager) {
        this.s = manager.getSource("readm");
    }


    @Override
    public MangaDTO getManga(String url) throws Exception {
        MangaDTO mndt = new MangaDTO();
        mndt.setURL(url);
        mndt.setDescription("");
        mndt.setSource(s);
        Set<String> restrictedNames = new HashSet<>();
        restrictedNames.add("N/A");
        try {
            Document doc = Jsoup.connect(url).get();
            Optional.ofNullable(doc.selectFirst("h1.page-title")).map(t -> t.text().strip()).ifPresentOrElse(t -> {
                mndt.setPrimaryTitle(t);
                mndt.getTitles().add(t);
            }, () -> {
                throw new RuntimeException("No Title Found");
            });
            Optional.ofNullable(doc.selectFirst("img.series-profile-thumb")).ifPresentOrElse(t -> mndt.setCoverURL(t.attr("abs:src")), () -> {
                throw new RuntimeException("No Cover Url Found");
            });
            Optional.ofNullable(doc.selectFirst("div.sub-title")).ifPresent(t -> extractWithTrim(t.text(), mndt.getTitles(), restrictedNames, ',', ';'));
            Optional.ofNullable(doc.selectFirst("div.series-summary-wrapper")).ifPresent(t -> {
                t.getElementsByTag("p").forEach(f -> mndt.setDescription(mndt.getDescription() + f.text()));
                mndt.setDescription(mndt.getDescription().strip());
                t.getElementsByTag("a").forEach(f -> mndt.getGenres().add(f.text()));
            });
            Optional.ofNullable(doc.selectFirst("series-status")).ifPresentOrElse(t -> mndt.setStatus(t.text()), () -> mndt.setStatus("Not Available"));
            Optional.ofNullable(doc.selectFirst("span.first-episode > a")).ifPresent(t -> mndt.getAuthors().add(t.text()));
            Optional.ofNullable(doc.selectFirst("span.last-episode > a")).ifPresent(t -> mndt.getArtists().add(t.text()));
            Elements ls = doc.select("td.table-episodes-title a");
            for (int i = 0; i < ls.size(); i++) {
                mndt.getChapters().add(getChapter(ls.size() - i - 1, ls.get(i).attr("abs:href"), url));
            }
        } catch (Exception ex) {
            System.out.println("Happened Here: " + mndt);
            throw ex;
        }
        return mndt;
    }

    public ChapterDTO getChapter(Integer sequenceNumber, String url, String mURL) {
        ChapterDTO cdto = new ChapterDTO();
        cdto.setChapterName("");
        cdto.setChapterNumber("");
        cdto.setSequenceNumber(sequenceNumber);
        try {
            Document doc = Jsoup.connect(url).get();
            Optional.ofNullable(doc.selectFirst("div.media-date")).ifPresent(t -> cdto.setUpdatedAt(DTF.parse(t.text(), Instant::from)));
            Optional.ofNullable(doc.selectFirst("span.light-title")).ifPresent(t -> cdto.setChapterNumber(t.text().substring(8).strip()));
            doc.select("img.img-responsive").forEach(t -> cdto.getImagesURL().add(t.attr("abs:src")));
        } catch (Exception ex) {
            ex.printStackTrace();
            pFunc.accept(mURL);
        }
        return cdto;
    }

    @Override
    public Integer getNumberOfPages() {
        return TOTAL_PAGES;
    }

    @Override
    public void getLiterallyEveryLink(int x, Consumer<String> func) {
        try {
            Document doc = Jsoup.connect(SEARCH_ALL + "/" + ((x == 1) ? "" : (char) (x + 95))).get();
            doc.select("div.poster-xs").forEach(t -> func.accept(t.selectFirst("a").attr("abs:href")));
        } catch (IOException e) {
            e.printStackTrace();
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
            Document doc = Jsoup.connect(ALL_GENRE).get();
            doc.select("ul.advanced-search-categories").forEach(t -> t.getElementsByTag("li").stream().map(f -> f.text()).forEach(p -> ls.add(p)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ls;
    }

    @Override
    public void acceptOnProblem(Consumer<String> func) {
        this.pFunc = func;
    }
}
