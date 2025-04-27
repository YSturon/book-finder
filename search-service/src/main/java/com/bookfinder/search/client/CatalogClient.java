package com.bookfinder.search.client;

import com.bookfinder.common.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "catalogClient", url = "http://localhost:8081")
public interface CatalogClient {

    @PostMapping(
            value    = "/internal/batch",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void saveBatch(@RequestBody List<BookDto> books);

}
