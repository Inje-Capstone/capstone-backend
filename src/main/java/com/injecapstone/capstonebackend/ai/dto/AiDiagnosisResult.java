package com.injecapstone.capstonebackend.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// AI POST /api/backend/onboarding/diagnosis 응답 중 백엔드가 쓰는 필드만 (가중치 등은 무시).
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiDiagnosisResult(
        int correctCount,
        int totalCount,
        String learningLevel,
        List<QuestionResult> results
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QuestionResult(
            int questionId,
            Integer chosenOptionId,
            boolean correct
    ) {
    }
}
