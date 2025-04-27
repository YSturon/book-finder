package com.bookfinder.catalog.service.impl;

import com.bookfinder.common.dto.BookDto;
import com.bookfinder.catalog.mapper.BookMapper;
import com.bookfinder.catalog.model.BookEntity;
import com.bookfinder.catalog.repository.BookRepository;
import com.bookfinder.catalog.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository repo;
    private final BookMapper mapper;

    @Override
    public List<BookDto> findAll() {
        return repo.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public BookDto findById(Long id) {
        BookEntity entity = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book " + id + " not found"));
        return mapper.toDto(entity);
    }

    @Override
    public BookDto create(BookDto dto) {
        // 1. Маппинг DTO → Entity
        BookEntity entity = mapper.toEntity(dto);

        // 2. Заполняем дефолты, если что-то отсутствует
        fillDefaults(entity);

        // 3. Ищем существующую книгу по title + author (игнорируя регистр)
        Optional<BookEntity> existing = repo.findFirstByTitleIgnoreCaseAndAuthorIgnoreCase(
                entity.getTitle(), entity.getAuthor()
        );

        // 4. Если нашли существующую книгу — возвращаем её DTO
        if (existing.isPresent()) {
            return mapper.toDto(existing.get());
        }

        // 5. Если не нашли — сохраняем новую книгу
        BookEntity saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    // Заполнение дефолтных значений
    private void fillDefaults(BookEntity e) {
        if (e.getAuthor() == null || e.getAuthor().isBlank()) e.setAuthor("Unknown author");
        if (e.getTitle() == null || e.getTitle().isBlank()) e.setTitle("Untitled");
        if (e.getSource() == null || e.getSource().isBlank()) e.setSource("unknown");
        if (e.getParsedAt() == null) e.setParsedAt(Instant.now());
    }


    @Override
    public BookDto update(Long id, BookDto dto) {
        BookEntity existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book " + id + " not found"));
        mapper.updateEntity(existing, dto);
        return mapper.toDto(repo.save(existing));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
