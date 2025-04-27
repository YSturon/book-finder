package com.bookfinder.search.client.controller;

import com.bookfinder.common.dto.BookDto;
import com.bookfinder.search.dto.SearchRequestDto;
import com.bookfinder.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bookfinder.search.client.CatalogClient;


import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService service;
    private final CatalogClient catalogClient;   // <-- добавили сюда

    @PostMapping
    public ResponseEntity<List<BookDto>> search(@RequestBody SearchRequestDto req) {
        List<BookDto> books = service.search(req.getAuthor(), req.getTitle())
                .collectList()
                .block();

        if (books != null && !books.isEmpty()) {
            catalogClient.saveBatch(books);  // <-- теперь этот код компилируется
        }

        return ResponseEntity.ok(books);
    }
}
