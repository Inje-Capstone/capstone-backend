package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import jakarta.validation.constraints.NotNull;

public record LearningLevelUpdateRequest(
        @NotNull(message = "학습 수준을 선택해 주세요.")
        LearningLevel learningLevel
) {
}