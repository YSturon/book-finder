package com.bookfinder.catalog.mapper;

import com.bookfinder.catalog.dto.BookDto;
import com.bookfinder.catalog.model.BookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookDto toDto(BookEntity entity);
    BookEntity toEntity(BookDto dto);

    // для PATCH/PUT
    void updateEntity(@MappingTarget BookEntity entity, BookDto dto);
}
