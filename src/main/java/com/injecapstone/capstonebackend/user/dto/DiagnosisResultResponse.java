package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.LearningLevel;

public record DiagnosisResultResponse(
        int correctCount,
        int totalCount,
        LearningLevel learningLevel
) {
}