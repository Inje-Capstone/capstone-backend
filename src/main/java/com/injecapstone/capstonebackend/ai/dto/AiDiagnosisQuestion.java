package com.injecapstone.capstonebackend.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// AI GET /api/backend/onboarding/questions — 정답은 담기지 않는다.
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiDiagnosisQuestion(
        int questionId,
        String termId,
        String question,
        List<AiOption> options
) {
}
