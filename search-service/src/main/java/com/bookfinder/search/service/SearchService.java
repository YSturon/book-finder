package com.bookfinder.search.service;

import com.bookfinder.common.dto.BookDto;
import com.bookfinder.search.provider.BookProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final List<BookProvider> providers;

    public Flux<BookDto> search(String author, String title) {
        AtomicInteger wikipediaCount = new AtomicInteger();
        AtomicInteger openLibraryCount = new AtomicInteger();
        AtomicInteger gutendexCount = new AtomicInteger();

        return Flux.fromIterable(providers)
                .flatMap(p -> p.search(author, title))
                .filter(book -> {
                    boolean titleOk = book.getTitle() != null && !book.getTitle().isBlank();
                    boolean authorOk = book.getAuthor() != null && !book.getAuthor().isBlank();

                    if ("wikipedia".equalsIgnoreCase(book.getSource())) {
                        if (titleOk) {
                            log.info("Keeping Wikipedia book: {}", book.getTitle());
                            return true;
                        } else {
                            log.info("Dropping Wikipedia book (bad title): {}", book.getTitle());
                            return false;
                        }
                    } else {
                        if (titleOk && authorOk) {
                            log.info("Keeping {} book: {}", book.getSource(), book.getTitle());
                            return true;
                        } else {
                            log.info("Dropping {} book (bad title or author): {}", book.getSource(), book.getTitle());
                            return false;
                        }
                    }
                })
                .doOnNext(book -> {
                    switch (book.getSource().toLowerCase()) {
                        case "wikipedia" -> wikipediaCount.incrementAndGet();
                        case "openlibrary" -> openLibraryCount.incrementAndGet();
                        case "gutendex" -> gutendexCount.incrementAndGet();
                    }
                })
                .distinct(this::dedupKey)
                .doOnComplete(() -> log.info("Books summary: Wikipedia={}, OpenLibrary={}, Gutendex={}",
                        wikipediaCount.get(), openLibraryCount.get(), gutendexCount.get()));
    }

    private String dedupKey(BookDto book) {
        return (book.getTitle() + "::" + book.getAuthor()).toLowerCase();
    }
}
