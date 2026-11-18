package com.injecapstone.capstonebackend.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// AI GET /api/games/{id}/today-rules — 이 경기에서 그 시각까지 카드로 본 용어 키
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiTodayRules(
        @JsonProperty("term_ids")
        List<String> termIds
) {
}
