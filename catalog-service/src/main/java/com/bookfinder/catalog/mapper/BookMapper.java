package com.bookfinder.catalog.mapper;

import com.bookfinder.catalog.model.BookEntity;
import com.bookfinder.common.dto.BookDto;
import org.mapstruct.*;

import java.time.Instant;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    /* ---------- DTO → Entity ---------- */
    BookEntity toEntity(BookDto dto);

    /* ---------- PATCH Entity ---------- */
    void updateEntity(@MappingTarget BookEntity entity, BookDto dto);

    /* ---------- Entity → DTO ---------- */
    BookDto toDto(BookEntity entity);

    /* ---------- post-processing ---------- */
    @AfterMapping
    default void fillDefaults(@MappingTarget BookEntity e) {
        if (e.getAuthor()  == null || e.getAuthor().isBlank())  e.setAuthor("Unknown author");
        if (e.getTitle()   == null || e.getTitle().isBlank())   e.setTitle("Untitled");
        if (e.getSource()  == null || e.getSource().isBlank())  e.setSource("unknown");
        if (e.getParsedAt()== null)                             e.setParsedAt(Instant.now());
    }
}
