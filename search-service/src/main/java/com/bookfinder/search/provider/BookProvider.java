package com.bookfinder.search.provider;

import com.bookfinder.common.dto.BookDto;
import reactor.core.publisher.Flux;

public interface BookProvider {
    Flux<BookDto> search(String author, String title);
}