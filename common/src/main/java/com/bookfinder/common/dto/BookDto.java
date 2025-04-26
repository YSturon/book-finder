package com.bookfinder.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@JsonDeserialize(builder = BookDto.BookDtoBuilder.class)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class BookDto {

    String  title;
    String  author;
    Integer year;
    String  genre;
    String  summary;
    String  coverUrl;
    String  source;
    String  sourceUrl;
    Instant parsedAt;

    @JsonPOJOBuilder(withPrefix = "")
    public static class BookDtoBuilder {}
}
