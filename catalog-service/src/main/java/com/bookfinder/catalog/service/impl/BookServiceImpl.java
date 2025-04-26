package com.bookfinder.catalog.service.impl;

import com.bookfinder.common.dto.BookDto;
import com.bookfinder.catalog.mapper.BookMapper;
import com.bookfinder.catalog.model.BookEntity;
import com.bookfinder.catalog.repository.BookRepository;
import com.bookfinder.catalog.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        BookEntity entity = mapper.toEntity(dto);
        return mapper.toDto(repo.save(entity));
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
