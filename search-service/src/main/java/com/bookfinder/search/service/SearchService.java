package com.bookfinder.search.service;

import com.bookfinder.common.dto.BookDto;
import com.bookfinder.search.client.CatalogClient;
import com.bookfinder.search.provider.BookProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final List<BookProvider> providers;
    private final CatalogClient      catalogClient;

    public List<BookDto> search(String author, String title) {

        Flux<BookDto> pipeline = Flux.fromIterable(providers)
                .flatMap(p -> p.search(author, title));

        /* --- «умягчаем» проверку, если заданы ОБА параметра --- */
        if (author != null && !author.isBlank() &&
                title  != null && !title .isBlank()) {

            final String reqTitle  = normalize(title);
            final String reqAuthor = normalize(author);

            pipeline = pipeline.filter(b -> {
                /* проверяем название */
                String bookTitle = normalize(b.getTitle());
                boolean titleOk  = bookTitle.contains(reqTitle) || reqTitle.contains(bookTitle);

                /* проверяем автора – только если он распаршен */
                boolean authorOk = true;
                if (b.getAuthor() != null && !b.getAuthor().isBlank()) {
                    String bookAuthor = normalize(b.getAuthor());
                    authorOk = bookAuthor.contains(reqAuthor) || reqAuthor.contains(bookAuthor);
                }
                return titleOk && authorOk;
            });
        }

        /* --- финальные проверки и дедупликация --- */
        List<BookDto> books = pipeline
                .filter(b -> b.getTitle() != null && !b.getTitle().isBlank())
                .distinct(b -> (normalize(b.getTitle()) + "|" +
                        (b.getAuthor() == null ? "" : normalize(b.getAuthor()))))
                .collectList()
                .block();

        log.info("Books to save: {}", books);
        if (!books.isEmpty()) {
            catalogClient.saveBatch(books);
        }
        return books;
    }

    /* helper ------------------------------------------------------------- */
    private static String normalize(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)  // убираем диакритику
                .replaceAll("\\p{M}", "")                   // "
                .replaceAll("[\\p{Punct}\\s]+", " ")        // точки/запятые/лишние пробелы
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
