package com.injecapstone.capstonebackend.glossary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/glossary")
@SecurityRequirement(name = "bearerAuth")
public class GlossaryController {

    private static final ParameterizedTypeReference<List<Map<String, Object>>> TERM_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient aiClient;

    public GlossaryController(
            @Value("${LOOKIE_AI_BASE_URL:http://127.0.0.1:8000}") String aiBaseUrl
    ) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2_000);
        requestFactory.setReadTimeout(5_000);

        this.aiClient = RestClient.builder()
                .baseUrl(aiBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Operation(summary = "야구 용어 목록 조회")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> listTerms(
            @AuthenticationPrincipal Long userId
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            var response = aiClient.get()
                    .uri("/api/glossary")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(TERM_LIST);

            var terms = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || terms == null) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            return ResponseEntity.ok(terms);
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}