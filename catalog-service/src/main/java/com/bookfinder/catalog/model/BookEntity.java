package com.bookfinder.catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "books",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_book_unique",
                columnNames = { "title", "author", "publish_year", "source" }
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

    @Column(name = "cover_url", length = 1024)
    private String coverUrl;

    @Column(nullable = false, length = 64)
    private String source;          // openlibrary / wikipedia …

    @Column(name = "source_url", length = 1024)   // NEW
    private String sourceUrl;

    @Column(name = "parsed_at")
    private Instant parsedAt;
}
