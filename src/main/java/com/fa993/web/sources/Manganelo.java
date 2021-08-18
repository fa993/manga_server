package com.fa993.web.sources;

import com.fa993.core.exceptions.MangaFetchingException;
import com.fa993.core.exceptions.PageProcessingException;
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

	private static final DateTimeFormatter FMT = new DateTimeFormatterBuilder().appendPattern("MMM dd,yyyy - HH:mm")
			.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).toFormatter().withZone(ZoneId.systemDefault());
	private static final DateTimeFormatter FMTC = new DateTimeFormatterBuilder().appendPattern("MMM dd,yyyy HH:mm")
			.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).toFormatter().withZone(ZoneId.systemDefault());

	private static final String SEARCH_STRING = "https://manganelo.com/search/story/";
	private static final String SEARCH_ALL = "https://manganelo.com/advanced_search?s=all&orby=az&page=";
	private static final String ALL_GENRE = "https://manganelo.com/genre-all";

	private final Integer noOfPages;

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

	public MangaDTO getManga(String url) throws MangaFetchingException {
		boolean error = false;
		Exception e = null;
		MangaDTO mndt = new MangaDTO();
		mndt.setURL(url);
		mndt.setDescription("");
		mndt.setSource(s);
		Set<String> empty = new HashSet<>();
		try {
			Document el = Jsoup.connect(url).get();
			Optional.ofNullable(el.selectFirst("div.story-info-right > h1")).map(t -> t.text().strip())
					.ifPresentOrElse(t -> {
						mndt.setPrimaryTitle(t);
						mndt.getTitles().add(t);
					}, () -> {
						throw new RuntimeException("No title");
					});
			Optional.ofNullable(el.selectFirst("span.info-image > img"))
					.ifPresentOrElse(t -> mndt.setCoverURL(t.attr("src")), () -> {
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
					e = ex;
					error = true;
				}
				ChapterDTO cdt = new ChapterDTO();
				cdt.setChapterName(chapName);
				cdt.setChapterNumber(chapNumber);
				cdt.setSequenceNumber(ls.size() - i - 1);
				Optional.ofNullable(t2.attr("title")).ifPresent(t -> cdt.setUpdatedAt(FMTC.parse(t, Instant::from)));
				try {
					cdt.setImagesURL(getImages(t1.attr("href"), url));
				} catch (Exception ex) {
					ex.printStackTrace();
					error = true;
					e = ex;
				}
				mndt.getChapters().add(cdt);
			}
		} catch (Exception ex) {
			System.out.println("Happened Here: " + mndt);
			throw new MangaFetchingException(url, mndt, ex);
		}
		if (error) {
			throw new MangaFetchingException(url, mndt, e);
		}
		return mndt;
	}

	private List<String> getImages(String url, String mURL) throws Exception {
		return Jsoup.connect(url).get().select("div.container-chapter-reader > img").stream().map(t -> t.attr("src"))
				.toList();
	}
	
	@Override
	public Integer getCompleteNumberOfPages() {
		return this.noOfPages;
	}

	@Override
	public void getLiterallyEveryLink(int x, Consumer<String> onProcessed) throws PageProcessingException {
		try {
			Document doc = Jsoup.connect(SEARCH_ALL + "/" + x).get();
			for (Element t : doc.select("div.panel-content-genres > div.content-genres-item > a.genres-item-img")) {
				onProcessed.accept(t.attr("href"));
			}
		} catch (IOException e) {
			throw new PageProcessingException(x, this.getSource(), e);
		}
	}
	
	@Override
	public Integer getNumberOfPagesToWatch() {
		return null;
	}

	@Override
	public void watch(int x, Consumer<String> func) throws MangaFetchingException {

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

	private Integer loadElement() {
		Integer ret = null;
		try {
			Document doc = Jsoup.connect(SEARCH_ALL).get();
			ret = Integer.parseInt(doc.selectFirst("a.page-last").text().substring(5, 9));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

}
