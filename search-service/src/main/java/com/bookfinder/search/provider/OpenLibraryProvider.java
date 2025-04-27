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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class OpenLibraryProvider implements BookProvider {

    private static final String BASE = "https://openlibrary.org";

    private final WebClient client = WebClient.builder()
            .baseUrl(BASE)
            .build();

    @Override
    public Flux<BookDto> search(String author, String title) {

        if ((author == null || author.isBlank()) &&
                (title  == null || title .isBlank())) {
            return Flux.empty();
        }

        String q = Stream.of(title, author)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));

        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("q", q.isBlank() ? "*:*" : q)
                        .queryParam("limit", 10)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMapMany(json -> {
                    var docs = json.path("docs");
                    if (docs.isArray() && docs.size() == 0 &&
                            title != null && !title.isBlank()) {
                        return client.get()
                                .uri("/search.json?title={t}&limit=10", title)
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .flatMapMany(j -> Flux.fromIterable(j.path("docs")));
                    }
                    return Flux.fromIterable(docs);
                })
                .flatMap(this::enrichAndBuildDto)
                .onErrorResume(e -> {
                    log.warn("OpenLibrary error: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    /* ---------- helpers ---------- */

    private Mono<BookDto> enrichAndBuildDto(JsonNode doc) {

        String author = blankToNull(
                doc.hasNonNull("author_name") && !doc.get("author_name").isEmpty()
                        ? doc.get("author_name").get(0).asText()
                        : null);

        Integer year = doc.path("first_publish_year").isInt()
                ? doc.get("first_publish_year").asInt()
                : null;

        String coverUrl = blankToNull(
                doc.hasNonNull("cover_i")
                        ? "https://covers.openlibrary.org/b/id/" + doc.get("cover_i").asText() + "-L.jpg"
                        : null);

        String workKey   = doc.path("key").asText(null);
        String sourceUrl = blankToNull(workKey != null ? BASE + workKey : null);

        Mono<JsonNode> workMono = workKey == null
                ? Mono.just(NullNode.getInstance())
                : client.get()
                .uri(workKey + ".json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(e -> Mono.just(NullNode.getInstance()));

        return workMono.map(work -> {

            String genre = blankToNull(
                    work.hasNonNull("subjects") && work.get("subjects").size() > 0
                            ? work.get("subjects").get(0).asText()
                            : null);

            String summary = null;
            if (work.hasNonNull("description")) {
                JsonNode desc = work.get("description");
                summary = desc.isTextual() ? desc.asText()
                        : desc.hasNonNull("value") ? desc.get("value").asText()
                        : null;
            }
            if (summary == null) {
                summary = doc.path("first_sentence").asText(null);
            }
            summary = blankToNull(summary);

            return BookDto.builder()
                    .title(blankToNull(doc.path("title").asText(null)))
                    .author(author)
                    .year(year)
                    .genre(genre)
                    .summary(summary)
                    .coverUrl(coverUrl)
                    .source("openlibrary")
                    .sourceUrl(sourceUrl)
                    .parsedAt(Instant.now())
                    .build();
        });
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}