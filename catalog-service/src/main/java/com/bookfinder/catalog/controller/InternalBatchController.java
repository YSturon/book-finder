package com.bookfinder.catalog.controller;


import com.bookfinder.catalog.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.bookfinder.common.dto.BookDto;


import java.util.List;

@RestController
@RequestMapping("/internal")              // общий префикс для «внутренних» вызовов
@RequiredArgsConstructor
public class InternalBatchController {

    private final BookService bookService;    // внедряется Spring’ом

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveBatch(@RequestBody List<BookDto> list) {
        list.forEach(bookService::create);    // один за другим сохраняем книги
    }
}
