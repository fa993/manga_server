package com.fa993.web.sources;

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

	private static final DateTimeFormatter DTF = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy")
			.parseDefaulting(ChronoField.NANO_OF_DAY, 0).toFormatter().withZone(ZoneId.systemDefault());

	private final Source s;

	public ReadM(SourceManager manager) {
		this.s = manager.getSource("readm", 1);
	}

	@Override
	public MangaDTO getManga(String url) throws MangaFetchingException {
		Exception e = null;
		boolean error = false;
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
			Optional.ofNullable(doc.selectFirst("img.series-profile-thumb"))
					.ifPresentOrElse(t -> mndt.setCoverURL(t.attr("abs:src")), () -> {
						throw new RuntimeException("No Cover Url Found");
					});
			Optional.ofNullable(doc.selectFirst("div.sub-title"))
					.ifPresent(t -> extractWithTrim(t.text(), mndt.getTitles(), restrictedNames, ',', ';'));
			Optional.ofNullable(doc.selectFirst("div.series-summary-wrapper")).ifPresent(t -> {
				t.getElementsByTag("p").forEach(f -> mndt.setDescription(mndt.getDescription() + f.text()));
				mndt.setDescription(mndt.getDescription().strip());
				t.getElementsByTag("a").forEach(f -> mndt.getGenres().add(f.text()));
			});
			Optional.ofNullable(doc.selectFirst("series-status")).ifPresentOrElse(t -> mndt.setStatus(t.text()),
					() -> mndt.setStatus("Not Available"));
			Optional.ofNullable(doc.selectFirst("span.first-episode > a"))
					.ifPresent(t -> mndt.getAuthors().add(t.text()));
			Optional.ofNullable(doc.selectFirst("span.last-episode > a"))
					.ifPresent(t -> mndt.getArtists().add(t.text()));
			Elements ls = doc.select("td.table-episodes-title a");
			for (int i = 0; i < ls.size(); i++) {
				try {
					mndt.getChapters().add(getChapter(ls.size() - i - 1, ls.get(i).attr("abs:href"), url));
				} catch (Exception ex) {
					e = ex;
					error = true;
				}
			}
		} catch (Exception ex) {
			System.out.println("Happened Here: " + mndt);
			throw new MangaFetchingException(url, null, ex);
		}
		if(error) {
			throw new MangaFetchingException(url, mndt, e);
		}
		return mndt;
	}

	public ChapterDTO getChapter(Integer sequenceNumber, String url, String mURL) throws Exception {
		ChapterDTO cdto = new ChapterDTO();
		cdto.setChapterName("");
		cdto.setChapterNumber("");
		cdto.setSequenceNumber(sequenceNumber);
		Document doc = Jsoup.connect(url).get();
		Optional.ofNullable(doc.selectFirst("div.media-date"))
				.ifPresent(t -> cdto.setUpdatedAt(DTF.parse(t.text(), Instant::from)));
		Optional.ofNullable(doc.selectFirst("span.light-title"))
				.ifPresent(t -> cdto.setChapterNumber(t.text().substring(8).strip()));
		doc.select("img.img-responsive").forEach(t -> cdto.getImagesURL().add(t.attr("abs:src")));
		return cdto;
	}

	@Override
	public Integer getCompleteNumberOfPages() {
		return TOTAL_PAGES;
	}

	@Override
	public void getLiterallyEveryLink(int x, Consumer<String> onProcessed) throws PageProcessingException {
		try {
			Document doc = Jsoup.connect(SEARCH_ALL + "/" + ((x == 1) ? "" : (char) (x + 95))).get();
			doc.select("div.poster-xs").forEach(t -> onProcessed.accept(t.selectFirst("a").attr("abs:href")));
		} catch (IOException e) {
			throw new PageProcessingException(x, this.getSource(), e);
		}
	}

	@Override
	public Integer getNumberOfPagesToWatch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void watch(int x, Consumer<String> onProcessed) throws PageProcessingException {

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
			doc.select("ul.advanced-search-categories")
					.forEach(t -> t.getElementsByTag("li").stream().map(f -> f.text()).forEach(p -> ls.add(p)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ls;
	}

}
