package com.bookfinder.catalog.service;

import com.bookfinder.catalog.dto.BookDto;

import java.util.List;

public interface BookService {
    List<BookDto> findAll();
    BookDto findById(Long id);
    BookDto create(BookDto dto);
    BookDto update(Long id, BookDto dto);
    void delete(Long id);
}
