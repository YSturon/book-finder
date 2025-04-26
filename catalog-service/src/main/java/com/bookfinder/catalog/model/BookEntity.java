package com.bookfinder.catalog.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity     // таблица books уже создана Flyway
@Table(name = "books",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_book_unique",
                columnNames = { "title", "author", "publish_year" }
        ))
public class BookEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(nullable = false, length = 512)
    private String author;

    @Column(name = "publish_year")
    private Integer publishYear;

    private String genre;

    @Column(columnDefinition = "text")
    private String summary;
}