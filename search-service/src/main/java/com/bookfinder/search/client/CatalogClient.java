package com.bookfinder.search.client;

import com.bookfinder.common.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "catalogClient", url = "http://localhost:8081")
public interface CatalogClient {

    @PostMapping("/internal/batch")
    void saveBatch(List<BookDto> list);
}
