package com.bookfinder.search.provider;

import com.bookfinder.common.dto.BookDto;
import com.fasterxml.jackson.databind.JsonNode;
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
public class GutendexProvider implements BookProvider {

    private static final String BASE = "https://gutendex.com";

    private final WebClient client = WebClient.builder()
            .baseUrl(BASE)
            .build();

    @Override
    public Flux<BookDto> search(String author, String title) {
        if ((author == null || author.isBlank()) &&
                (title  == null || title.isBlank())) {
            return Flux.empty();
        }

        String q = Stream.of(title, author)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));

        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/books")
                        .queryParam("search", q.isBlank() ? "" : q)
                        .queryParam("page", 1)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMapMany(json -> Flux.fromIterable(json.path("results")))
                .flatMap(this::mapToDto)
                .onErrorResume(e -> {
                    log.warn("Gutendex error: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    private Mono<BookDto> mapToDto(JsonNode node) {
        String title = blankToNull(node.path("title").asText(null));

        String author = null;
        if (node.hasNonNull("authors") && node.get("authors").isArray() && node.get("authors").size() > 0) {
            author = blankToNull(node.get("authors").get(0).path("name").asText(null));
        }

        String genre = null;
        if (node.hasNonNull("subjects") && node.get("subjects").isArray() && node.get("subjects").size() > 0) {
            genre = blankToNull(node.get("subjects").get(0).asText(null));
        }

        String coverUrl = null;
        JsonNode formats = node.path("formats");
        if (formats.hasNonNull("image/jpeg")) {
            coverUrl = blankToNull(formats.get("image/jpeg").asText(null));
        }

        // Получение описания из summaries
        String summary = null;
        if (node.hasNonNull("summaries") && node.get("summaries").isArray() && node.get("summaries").size() > 0) {
            summary = blankToNull(node.get("summaries").get(0).asText(null));
        }

        String id = node.path("id").asText(null);
        String sourceUrl = id != null
                ? "https://www.gutenberg.org/ebooks/" + id
                : null;

        return Mono.just(BookDto.builder()
                .title(title)
                .author(author)
                .year(null)               // в Gutendex нет года публикации
                .genre(genre)
                .summary(summary)
                .coverUrl(coverUrl)
                .source("gutendex")
                .sourceUrl(blankToNull(sourceUrl))
                .parsedAt(Instant.now())
                .build());
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
