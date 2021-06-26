package com.fa993.scrapper.sources;

import com.fa993.core.managers.SourceManager;
import com.fa993.core.pojos.Source;
import com.fa993.retrieval.SourceScrapper;
import com.fa993.retrieval.Scrapper;
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

@Scrapper
public class Manganelo implements SourceScrapper {

    private final Source s;

    private static final String AUTHOR = "Author(s) :";
    private static final String ALTERNATIVE_NAME = "Alternative :";
    private static final String STATUS = "Status :";
    private static final String GENRES = "Genres :";
    private static final String UPDATED = "Updated :";

    private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder().appendPattern("MMM dd,yyyy - HH:mm").parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).toFormatter().withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FMTC = new DateTimeFormatterBuilder().appendPattern("MMM dd,yyyy HH:mm").parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).toFormatter().withZone(ZoneId.systemDefault());

    private static final String SEARCH_STRING = "https://manganelo.com/search/story/";
    private static final String SEARCH_ALL = "https://manganelo.com/advanced_search?s=all&orby=az&page=";
    private static final String ALL_GENRE = "https://manganelo.com/genre-all";

    private final Integer noOfPages;

    private Consumer<String> pFunc;

    public Manganelo(SourceManager m) {
        s = m.getSource("manganelo");
        this.noOfPages = loadElement();
    }

    public List<MangaDTO> search(String key) {
        List<MangaDTO> mg = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(SEARCH_STRING + key).get();
            for (Element f : doc.select("div.search-story-item > a.item-img")) {
                MangaDTO mx = getManga(f.attr("href"));
                System.out.println("Done: " + mx.getPrimaryTitle());
                mg.add(mx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mg;
    }

    public MangaDTO getManga(String url) throws Exception {
        MangaDTO mndt = new MangaDTO();
        mndt.setURL(url);
        mndt.setDescription("");
        mndt.setSource(s);
        Set<String> empty = new HashSet<>();
        try {
            Document el = Jsoup.connect(url).get();
            Optional.ofNullable(el.selectFirst("div.story-info-right > h1")).map(t -> t.text().strip()).ifPresentOrElse(t -> {
                mndt.setPrimaryTitle(t);
                mndt.getTitles().add(t);
            }, () -> {
                throw new RuntimeException("No title");
            });
            Optional.ofNullable(el.selectFirst("span.info-image > img")).ifPresentOrElse(t -> mndt.setCoverURL(t.attr("src")), () -> {
                throw new RuntimeException("No Cover URL");
            });
            Elements labels = el.select("td.table-label");
            Elements values = el.select("td.table-value");
            for (int i = 0; i < labels.size(); i++) {
                switch (labels.get(i).text()) {
                    case AUTHOR -> extractWithTrim(values.get(i).text(), mndt.getAuthors(), empty, '-');
                    case ALTERNATIVE_NAME -> extractWithTrim(values.get(i).text(), mndt.getTitles(), empty, ',', ';');
                    case STATUS -> mndt.setStatus(values.get(i).text().toUpperCase());
                    case GENRES -> extractWithTrim(values.get(i).text(), mndt.getGenres(), empty, '-');
                    default -> System.out.println("Not recognized");
                }
            }
            Elements labels2 = el.select("span.stre-label");
            Elements values2 = el.select("span.stre-value");
            for (int i = 0; i < labels2.size(); i++) {
                switch (labels2.get(i).text()) {
                    case UPDATED:
                        String x = values2.get(i).text();
                        mndt.setLastUpdated(FMT.parse(x.substring(0, x.length() - 3), Instant::from));
                        break;
                    default:
                }
            }
            Optional.ofNullable(el.selectFirst(".panel-story-info-description")).ifPresent(t -> {
                mndt.setDescription(t.text().substring(13).strip());
            });
            Elements ls = el.select("a.chapter-name");
            Elements ls2 = el.select("span.chapter-time");
            for (int i = 0; i < ls.size(); i++) {
                Element t1 = ls.get(i);
                Element t2 = ls2.get(i);
                String x = t1.text();
                String chapName = "";
                String chapNumber = "";
                try {
                    int y = x.indexOf(':');
                    int chp = x.indexOf("Chapter");
                    if (chp < 0) {
                        chapName = x;
                    } else if (y > -1) {
                        chapName = x.substring(y + 2).strip();
                        chapNumber = x.substring(chp + "Chapter".length() + 1, y).strip();
                    } else {
                        chapNumber = x.substring(chp + "Chapter".length() + 1).strip();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Happened here: " + x + " with manga " + mndt);
                    chapName = x;
                    pFunc.accept(url);
                }
                ChapterDTO cdt = new ChapterDTO();
                cdt.setChapterName(chapName);
                cdt.setChapterNumber(chapNumber);
                cdt.setSequenceNumber(ls.size() - i - 1);
                Optional.ofNullable(t2.attr("title")).ifPresent(t -> cdt.setUpdatedAt(FMTC.parse(t, Instant::from)));
                cdt.setImagesURL(getImages(t1.attr("href"), url));
                mndt.getChapters().add(cdt);
            }
        } catch (Exception ex) {
            System.out.println("Happened Here: " + mndt);
            throw ex;
        }
        return mndt;
    }

//    public MangaRecord getMangaComplete(String url) throws Exception {
//        Document el = Jsoup.connect(url).get();
//        String upToPanel = "body > div.body-site > div.container-main > div.container-main-left > div.panel-story-info > ";
//        String primaryName = el.selectFirst(upToPanel + "div.story-info-right > h1").text();
//        Elements labels = el.select(upToPanel + "div.story-info-right > h1 > table.variations-tableInfo > tbody > tr > td.table-label");
//        Elements values = el.select(upToPanel + "div.story-info-right > h1 > table.variations-tableInfo > tbody > tr > td.table-value");
//        List<String> authors = null;
//        List<String> altNames = null;
//        String status = null;
//        List<String> genre = null;
//
//        String coverURL = el.selectFirst(upToPanel + "div.story-info-left > span.info-image > img").attr("src");
//
//        for (int i = 0; i < labels.size(); i++) {
//            switch (labels.get(i).text()) {
//                case AUTHOR:
//                    authors = extractWithTrim(values.get(i).text(), '-');
//                    break;
//                case ALTERNATIVE_NAME:
//                    altNames = extractWithTrim(values.get(i).text(), ',');
//                    break;
//                case STATUS:
//                    status = values.get(i).text().toUpperCase();
//                    break;
//                case GENRES:
//                    genre = extractWithTrim(values.get(i).text(), '-');
//                    break;
//                default:
//                    System.out.println("Not recognized");
//            }
//        }
//        Elements labels2 = el.select(upToPanel + "div.story-info-right > div.story-info-right-extent > p > span.stre-label");
//        Elements values2 = el.select(upToPanel + "div.story-info-right > div.story-info-right-extent > p > span.stre-value");
//        Instant lsUpdated = null;
//        for (int i = 0; i < labels2.size(); i++) {
//            switch (labels2.get(i).text()) {
//                case UPDATED:
//                    String x = values2.get(i).text();
//                    lsUpdated = FMT.parse(x.substring(0, x.length() - 3), Instant::from);
//                    break;
//                default:
//            }
//        }
//        String description = el.selectFirst(upToPanel + "div.panel-story-info-description").text().substring(13).strip();
//        Elements ls = el.select("body > div.body-site > div.container-main > div.container-main-left > div.panel-story-chapter-list > ul.row-content-chapter > li.a-h > a");
//        Elements ls2 = el.select("body > div.body-site > div.container-main > div.container-main-left > div.panel-story-chapter-list > ul.row-content-chapter > li.a-h > span.chapter-time");
//        List<ChapterRecord> c = new ArrayList<>(ls.size());
//        MangaRecord manga = new MangaRecord(primaryName, url, coverURL, c, authors, altNames, lsUpdated, description, genre, status);
//        for (int i = 1; i <= ls.size(); i++) {
//            Element t1 = ls.get(ls.size() - i);
//            Element t2 = ls2.get(ls2.size() - i);
//            String x = t1.text();
//            String chapName = null;
//            String chapNumber = null;
//            try {
//                int y = x.indexOf(':');
//                int chp = x.indexOf("Chapter");
//                if (chp < 0) {
//                    chapName = x;
//                } else if (y > -1) {
//                    chapName = x.substring(y + 2).strip();
//                    chapNumber = x.substring(chp + "Chapter".length() + 1, y).strip();
//                } else {
//                    chapNumber = x.substring(chp + "Chapter".length() + 1).strip();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.out.println("Happened here: " + x + " with manga " + manga);
//                chapName = x;
//            }
//            c.add(getChapterComplete(t1.attr("href"), chapName, chapNumber, FMTC.parse(t2.attr("title"), Instant::from)));
//        }
//        return manga;
//    }

//    public MangaRecord getMangaCompleteTest(File html) throws Exception {
//        Document el = Jsoup.parse(html, null);
//        String upToPanel = "body > div.body-site > div.container-main > div.container-main-left > div.panel-story-info > ";
//        String primaryName = el.selectFirst(upToPanel + "div.story-info-right > h1").text();
//        Elements labels = el.select(upToPanel + "div.story-info-right > h1 > table.variations-tableInfo > tbody > tr > td.table-label");
//        Elements values = el.select(upToPanel + "div.story-info-right > h1 > table.variations-tableInfo > tbody > tr > td.table-value");
//        List<String> authors = null;
//        List<String> altNames = null;
//        String status = null;
//        List<String> genre = null;
//
//        String coverURL = el.selectFirst(upToPanel + "div.story-info-left > span.info-image > img").attr("src");
//
//        for (int i = 0; i < labels.size(); i++) {
//            switch (labels.get(i).text()) {
//                case AUTHOR:
//                    authors = extractWithTrim(values.get(i).text(), '-');
//                    break;
//                case ALTERNATIVE_NAME:
//                    altNames = extractWithTrim(values.get(i).text(), ',');
//                    break;
//                case STATUS:
//                    status = values.get(i).text().toUpperCase();
//                    break;
//                case GENRES:
//                    genre = extractWithTrim(values.get(i).text(), '-');
//                    break;
//                default:
//                    System.out.println("Not recognized");
//            }
//        }
//        Elements labels2 = el.select(upToPanel + "div.story-info-right > div.story-info-right-extent > p > span.stre-label");
//        Elements values2 = el.select(upToPanel + "div.story-info-right > div.story-info-right-extent > p > span.stre-value");
//        Instant lsUpdated = null;
//        for (int i = 0; i < labels2.size(); i++) {
//            switch (labels2.get(i).text()) {
//                case UPDATED:
//                    String x = values2.get(i).text();
//                    lsUpdated = FMT.parse(x.substring(0, x.length() - 3), Instant::from);
//                    break;
//                default:
//            }
//        }
//        String description = el.selectFirst(upToPanel + "div.panel-story-info-description").text().substring(13).strip();
//        Elements ls = el.select("body > div.body-site > div.container-main > div.container-main-left > div.panel-story-chapter-list > ul.row-content-chapter > li.a-h > a");
//        Elements ls2 = el.select("body > div.body-site > div.container-main > div.container-main-left > div.panel-story-chapter-list > ul.row-content-chapter > li.a-h > span.chapter-time");
//        List<ChapterRecord> c = new ArrayList<>(ls.size());
//       MangaRecord manga = new MangaRecord(primaryName, null, coverURL, c, authors, altNames, lsUpdated, description, genre, status);
//        for (int i = 1; i <= ls.size(); i++) {
//            Element t1 = ls.get(ls.size() - i);
//            Element t2 = ls2.get(ls2.size() - i);
//            String x = t1.text();
//            String chapName = null;
//            String chapNumber = null;
//            try {
//                int y = x.indexOf(':');
//                int chp = x.indexOf("Chapter");
//                if (chp < 0) {
//                    chapName = x;
//                } else if (y > -1) {
//                    chapName = x.substring(y + 2).strip();
//                    chapNumber = x.substring(chp + "Chapter".length() + 1, y).strip();
//                } else {
//                    chapNumber = x.substring(chp + "Chapter".length() + 1).strip();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.out.println("Happened here: " + x + " with manga " + manga);
//                chapName = x;
//            }
////            c.add(getChapterComplete(t1.attr("href"), chapName, chapNumber, FMTC.parse(t2.attr("title"), Instant::from)));
//        }
//        return manga;
//    }

//    public MangaRecord getMangaTest(File html) throws Exception {
//        Document el = Jsoup.parse(html, null);
//        String primaryName = el.selectFirst("div.story-info-right > h1").text();
//        Elements labels = el.select("td.table-label");
//        Elements values = el.select("td.table-value");
//        List<String> authors = new ArrayList<>();
//        List<String> altNames = new ArrayList<>();
//        String status = null;
//        List<String> genre = new ArrayList<>();
//
//        String coverURL = el.selectFirst("span.info-image > img").attr("src");
//
//        for (int i = 0; i < labels.size(); i++) {
//            switch (labels.get(i).text()) {
//                case AUTHOR:
//                    extractWithTrim(values.get(i).text(), '-', authors);
//                    break;
//                case ALTERNATIVE_NAME:
//                    extractWithTrim(values.get(i).text(), ',', altNames);
//                    break;
//                case STATUS:
//                    status = values.get(i).text().toUpperCase();
//                    break;
//                case GENRES:
//                    extractWithTrim(values.get(i).text(), '-', genre);
//                    break;
//                default:
//                    System.out.println("Not recognized");
//            }
//        }
//        Elements labels2 = el.select("span.stre-label");
//        Elements values2 = el.select("span.stre-value");
//        Instant lsUpdated = null;
//        for (int i = 0; i < labels2.size(); i++) {
//            switch (labels2.get(i).text()) {
//                case UPDATED:
//                    String x = values2.get(i).text();
//                    lsUpdated = FMT.parse(x.substring(0, x.length() - 3), Instant::from);
//                    break;
//                default:
//            }
//        }
//        String description = el.selectFirst(".panel-story-info-description").text().substring(13).strip();
//        Elements ls = el.select("a.chapter-name");
//        Elements ls2 = el.select("span.chapter-time");
//        List<ChapterRecord> c = new ArrayList<>(ls.size());
//        MangaRecord manga = new MangaRecord(primaryName, null, coverURL, c, authors, altNames, lsUpdated, description, genre, status);
//        for (int i = 1; i <= ls.size(); i++) {
//            Element t1 = ls.get(ls.size() - i);
//            Element t2 = ls2.get(ls2.size() - i);
//            String x = t1.text();
//            String chapName = null;
//            String chapNumber = null;
//            try {
//                int y = x.indexOf(':');
//                int chp = x.indexOf("Chapter");
//                if (chp < 0) {
//                    chapName = x;
//                } else if (y > -1) {
//                    chapName = x.substring(y + 2).strip();
//                    chapNumber = x.substring(chp + "Chapter".length() + 1, y).strip();
//                } else {
//                    chapNumber = x.substring(chp + "Chapter".length() + 1).strip();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.out.println("Happened here: " + x + " with manga " + manga);
//                chapName = x;
//            }
////            c.add(getChapter(t1.attr("href"), chapName, chapNumber, FMTC.parse(t2.attr("title"), Instant::from)));
//        }
//        return manga;
//    }

    private List<String> getImages(String url, String mURL) {
        try {
            return Jsoup.connect(url).get().select("div.container-chapter-reader > img").stream().map(t -> t.attr("src")).toList();

        } catch (Exception ex) {
            ex.printStackTrace();
            pFunc.accept(mURL);
        }
        return new ArrayList<>();
    }

//    private ChapterRecord getChapterComplete(String url, String chapName, String chapNumber, Instant updatedAt) throws IOException {
//        Document doc = Jsoup.connect(url).get();
//        List<String> lsURL = new ArrayList<>();
//        doc.select("body > div.body-site > div.container-chapter-reader > img").forEach(t -> lsURL.add(t.attr("src")));
//        return new ChapterRecord(chapName, chapNumber, lsURL, updatedAt);
//    }

    private boolean getLiterallyEverything(int x, List<MangaDTO> mg) {
        try {
            Document doc = Jsoup.connect(SEARCH_ALL + "" + x).get();
            for (Element t : doc.select("div.panel-content-genres > div.content-genres-item > a.genres-item-img")) {
                long t1 = System.currentTimeMillis();
                MangaDTO mx = getManga(t.attr("href"));
                System.out.println("Done: " + mx.getPrimaryTitle());
                System.out.println("Time taken: " + (System.currentTimeMillis() - t1));
                mg.add(mx);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void getLiterallyEveryLink(int x, Consumer<String> func) {
        try {
            Document doc = Jsoup.connect(SEARCH_ALL + "/" + x).get();
            for (Element t : doc.select("div.panel-content-genres > div.content-genres-item > a.genres-item-img")) {
                long t1 = System.currentTimeMillis();
                func.accept(t.attr("href"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Source getSource() {
        return s;
    }

    @Override
    public List<String> getAllGenre() {
        List<String> output = new ArrayList<>();
        Document doc = null;
        try {
            doc = Jsoup.connect(ALL_GENRE).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Element t : doc.select("div.advanced-search-tool-genres-list > span")) {
            output.add(t.text().toLowerCase());
        }
        return output;
    }

    @Override
    public Integer getNumberOfPages() {
        return this.noOfPages;
    }

    private Integer loadElement(){
        Integer ret = null;
        try {
            Document doc = Jsoup.connect(SEARCH_ALL).get();
            ret = Integer.parseInt(doc.selectFirst("a.page-last").text().substring(5, 9));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public void acceptOnProblem(Consumer<String> func) {
        this.pFunc = func;
    }
}
