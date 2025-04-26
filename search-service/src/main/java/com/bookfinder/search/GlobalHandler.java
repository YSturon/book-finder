package com.bookfinder.search;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
// ОТЛАДКА!!!!
class GlobalHandler {
    @ExceptionHandler(Exception.class)
    ResponseEntity<String> any(Exception ex) {
        ex.printStackTrace();                 // ← всё равно пишем в консоль
        return ResponseEntity                // ← и отдаём текст клиенту
                .status(500)
                .body(ex.toString());
    }
}
