package com.injecapstone.capstonebackend.quiz.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuizAnswerRequest(
        @NotNull @Positive Integer optionId
) {
}