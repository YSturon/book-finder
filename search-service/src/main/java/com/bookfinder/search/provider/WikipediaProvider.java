package com.bookfinder.search.provider;

import com.bookfinder.common.dto.BookDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
            return Flux.empty();
        }

        String query = title.replace(' ', '_');

        Mono<JsonNode> summaryMono = client.get()
                .uri("/page/summary/{title}", query)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(e ->
                        client.get()
                                .uri("/page/search/{q}?limit=1", query)
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .flatMap(search ->
                                        search.path("pages").isArray() && search.path("pages").size() > 0
                                                ? client.get()
                                                .uri("/page/summary/{title}",
                                                        search.path("pages").get(0).path("key").asText())
                                                .retrieve()
                                                .bodyToMono(JsonNode.class)
                                                : Mono.just(NullNode.getInstance())))
                .defaultIfEmpty(NullNode.getInstance());

        Mono<JsonNode> metadataMono = client.get()
                .uri("/page/metadata/{title}", query)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(e ->
                        client.get()
                                .uri("/page/search/{q}?limit=1", query)
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .flatMap(search ->
                                        search.path("pages").isArray() && search.path("pages").size() > 0
                                                ? client.get()
                                                .uri("/page/metadata/{title}",
                                                        search.path("pages").get(0).path("key").asText())
                                                .retrieve()
                                                .bodyToMono(JsonNode.class)
                                                : Mono.just(NullNode.getInstance())))
                .defaultIfEmpty(NullNode.getInstance());

        return Mono.zip(summaryMono, metadataMono)
                .map(t -> buildDto(author, t.getT1(), t.getT2()))
                .flux()
                .onErrorResume(e -> {
                    log.warn("Wikipedia error: {}", e.toString());
                    return Flux.empty();
                });
    }

    /* ==================================================================== */

    private BookDto buildDto(String requestedAuthor,
                             JsonNode summary,
                             JsonNode meta) {

        String bookTitle   = blankToNull(summary.path("title").asText(null));
        String summaryText = blankToNull(summary.path("extract").asText(null));
        String sourceUrl   = blankToNull(summary.path("content_urls")
                .path("desktop")
                .path("page").asText(null));

        String genre = blankToNull(parseGenre(meta));
        Integer year = parseYear(meta);

        return BookDto.builder()
                .title(bookTitle)
                .author(blankToNull(requestedAuthor))
                .year(year)
                .genre(genre)
                .summary(summaryText)
                .coverUrl(null)
                .source("wikipedia")
                .sourceUrl(sourceUrl)
                .parsedAt(Instant.now())
                .build();
    }

    /* ==================================================================== */

    private String parseGenre(JsonNode meta) {
        if (!meta.hasNonNull("infoboxes")) return null;

        Pattern th   = Pattern.compile("<th[^>]*>\\s*Genre\\s*</th>", Pattern.CASE_INSENSITIVE);
        Pattern td   = Pattern.compile("<td[^>]*>(.*?)</td>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Pattern tags = Pattern.compile("<[^>]+>");

        for (JsonNode box : meta.get("infoboxes")) {
            String html = box.asText("");
            Matcher mTh = th.matcher(html);
            if (mTh.find()) {
                Matcher mTd = td.matcher(html.substring(mTh.end()));
                if (mTd.find()) {
                    String text = tags.matcher(mTd.group(1)).replaceAll("");
                    text = text.replace('\n', ' ').trim();
                    return blankToNull(text);
                }
            }
        }
        return null;
    }

    private Integer parseYear(JsonNode meta) {
        if (!meta.hasNonNull("infoboxes")) return null;
        Pattern year = Pattern.compile("\\b(1[89]\\d{2}|20\\d{2})\\b");
        for (JsonNode box : meta.get("infoboxes")) {
            Matcher m = year.matcher(box.asText(""));
            if (m.find()) {
                return Integer.valueOf(m.group());
            }
        }
        return null;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
