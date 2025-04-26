package com.bookfinder.search.dto;

import lombok.Data;

@Data
public class SearchRequestDto {
    private String author;   // может быть null
    private String title;    // может быть null, но хотя бы одно поле должно быть
}