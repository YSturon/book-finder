package com.bookfinder.catalog.repository;

import com.bookfinder.catalog.model.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
    Optional<BookEntity> findFirstByTitleIgnoreCaseAndAuthorIgnoreCase(String title, String author);
}
