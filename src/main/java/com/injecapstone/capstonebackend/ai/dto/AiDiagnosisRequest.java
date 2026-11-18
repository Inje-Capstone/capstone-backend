package com.injecapstone.capstonebackend.ai.dto;

import java.util.List;

// AI POST /api/backend/onboarding/diagnosis 요청 — 백엔드 DiagnosisSubmitRequest와 같은 모양.
public record AiDiagnosisRequest(
        List<Answer> answers
) {

    public record Answer(
            int questionId,
            int optionId
    ) {
    }
}
