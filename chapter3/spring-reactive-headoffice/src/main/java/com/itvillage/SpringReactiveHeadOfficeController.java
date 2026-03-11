package com.itvillage;

import com.itvillage.domain.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@Slf4j
@RequestMapping("/v1/books")
@RestController
public class SpringReactiveHeadOfficeController {
    URI baseUri = UriComponentsBuilder.newInstance().scheme("http")
            .host("localhost")
            .port(5050)
            .path("/v1/books")
            .build()
            .encode()
            .toUri();

//    @Autowired
    public SpringReactiveHeadOfficeController() {}

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{book-id}")
    public Mono<Book> getBook(@PathVariable("book-id") long bookId) {
        URI getBookUri = UriComponentsBuilder.fromUri(baseUri)
                .path("/{book-id}")
                .build()
                .expand(bookId)
                .encode()
                .toUri(); // http://localhost:5050/v1/books/{book-id}

        return WebClient.create()
                .get()
                .uri(getBookUri)
                .retrieve()
                .bodyToMono(Book.class);
    }
}
