package com.bookfinder.search.provider;

import com.bookfinder.common.dto.BookDto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class WikipediaProvider implements BookProvider {

    private final WebClient client = WebClient.builder()
            .baseUrl("https://en.wikipedia.org/api/rest_v1")
            .build();

    @Override
    public Flux<BookDto> search(String author, String title) {
        if (title == null || title.isBlank()) {
            log.info("WikipediaProvider: empty title provided, skipping search.");
            return Flux.empty();
        }

        String query = title.trim().replace(' ', '_');
        log.info("WikipediaProvider: searching for title '{}'.", query);

        return fetchSummary(query)
                .flatMap(summary -> {
                    if (summary == null || summary.isMissingNode()) {
                        log.warn("WikipediaProvider: summary is null or missing for '{}'.", query);
                        return Mono.empty();
                    }

                    BookDto baseDto = buildDto(author, summary);
                    String pageUrl = summary.path("content_urls").path("desktop").path("page").asText(null);

                    if (pageUrl != null) {
                        return enrichFromHtml(baseDto, pageUrl);
                    } else {
                        return Mono.just(baseDto);
                    }
                })
                .flux()
                .onErrorResume(e -> {
                    log.error("WikipediaProvider: error during search for '{}': {}", query, e.getMessage(), e);
                    return Flux.empty();
                });
    }

    private Mono<JsonNode> fetchSummary(String title) {
        return client.get()
                .uri("/page/summary/{title}", title)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(summary -> log.info("WikipediaProvider: fetched summary for '{}'.", title))
                .doOnError(e -> log.error("WikipediaProvider: failed to fetch summary for '{}': {}", title, e.getMessage()));
    }

    private BookDto buildDto(String requestedAuthor, JsonNode summary) {
        String bookTitle = blankToNull(summary.path("title").asText(null));
        String summaryText = blankToNull(summary.path("extract").asText(null));
        String sourceUrl = blankToNull(summary.path("content_urls").path("desktop").path("page").asText(null));
        String coverUrl = blankToNull(summary.path("originalimage").path("source").asText(null));

        BookDto dto = BookDto.builder()
                .title(bookTitle)
                .author(blankToNull(requestedAuthor))
                .year(null)
                .genre(null)
                .summary(summaryText)
                .coverUrl(coverUrl)
                .source("wikipedia")
                .sourceUrl(sourceUrl)
                .parsedAt(Instant.now())
                .build();

        log.info("WikipediaProvider: built base BookDto: {}", dto);
        return dto;
    }

    private Mono<BookDto> enrichFromHtml(BookDto book, String pageUrl) {
        return Mono.fromCallable(() -> {
            Document doc = Jsoup.connect(pageUrl).get();
            Element infobox = doc.selectFirst(".infobox");

            if (infobox != null) {
                String genre = infobox.select("th:contains(Genre) + td").text();
                String published = infobox.select("th:contains(Published) + td").text();

                Integer year = extractYear(published);

                BookDto enrichedBook = book.toBuilder()
                        .genre(blankToNull(genre))
                        .year(year)
                        .build();

                log.info("WikipediaProvider: enriched BookDto with genre '{}' and year '{}'.", genre, year);
                return enrichedBook;
            } else {
                log.info("WikipediaProvider: no infobox found at '{}'.", pageUrl);
                return book;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Integer extractYear(String text) {
        if (text == null) return null;
        Pattern yearPattern = Pattern.compile("\\b(1[89]\\d{2}|20\\d{2})\\b");
        Matcher matcher = yearPattern.matcher(text);
        if (matcher.find()) {
            return Integer.valueOf(matcher.group());
        }
        return null;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}