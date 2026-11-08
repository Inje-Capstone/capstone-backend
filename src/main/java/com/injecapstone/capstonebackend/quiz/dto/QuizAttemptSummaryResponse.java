package com.injecapstone.capstonebackend.quiz.dto;

public record QuizAttemptSummaryResponse(
        long totalCount,
        long correctCount,
        long incorrectCount
) {
}