package com.injecapstone.capstonebackend.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// AI GET /api/backend/quiz/questions — 고정 문제은행 문항. 정답은 담기지 않는다.
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiQuizQuestion(
        int questionId,
        String termId,
        String text,
        List<AiOption> options
) {
}
