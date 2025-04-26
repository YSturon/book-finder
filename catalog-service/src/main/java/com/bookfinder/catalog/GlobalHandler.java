package com.bookfinder.catalog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
// ОТЛАДКА
class GlobalHandler {
    @ExceptionHandler(Exception.class)
    ResponseEntity<String> any(Exception ex) {
        ex.printStackTrace();           // всё равно остаётся в логе
        return ResponseEntity.status(500).body(ex.toString());
    }
}
