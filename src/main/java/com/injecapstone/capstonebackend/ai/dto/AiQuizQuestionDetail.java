package com.injecapstone.capstonebackend.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// AI GET /api/backend/quiz/questions/{questionId} — 정답·해설 포함. 서버 안에서 채점에만 쓴다.
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiQuizQuestionDetail(
        int questionId,
        String termId,
        String text,
        List<AiOption> options,
        int correctOptionId,
        String explanation
) {
}
