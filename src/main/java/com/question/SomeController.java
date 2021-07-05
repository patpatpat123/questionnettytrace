package com.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@Controller
public class SomeController {

    @Autowired
    private WebClientServiceImpl webClientService;

    @PostMapping(path = "/testme")
    public Mono<ResponseEntity<String>> testme() {
        final var response = webClientService.sendPostRequest("");
        return response.map(ResponseEntity::ok);
    }

}
