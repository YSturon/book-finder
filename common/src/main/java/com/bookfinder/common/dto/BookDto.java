package com.bookfinder.common.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookDto {
    private Long id;
    private String title;
    private String author;
    private Integer publishYear;
    private String genre;
    private String summary;
}