package com.bookfinder.search.client.controller;

import com.bookfinder.common.dto.BookDto;
import com.bookfinder.search.dto.SearchRequestDto;
import com.bookfinder.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService service;

    @PostMapping
    public ResponseEntity<List<BookDto>> search(@RequestBody SearchRequestDto req) {
        List<BookDto> books = service.search(req.getAuthor(), req.getTitle())
                .collectList()
                .block();
        return ResponseEntity.ok(books);
    }
}