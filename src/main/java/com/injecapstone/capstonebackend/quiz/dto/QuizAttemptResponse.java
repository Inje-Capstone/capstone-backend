package com.injecapstone.capstonebackend.quiz.dto;

import com.injecapstone.capstonebackend.quiz.domain.QuizAttempt;

import java.time.LocalDateTime;

public record QuizAttemptResponse(
        int questionId,
        int selectedOptionId,
        boolean correct,
        LocalDateTime submittedAt,
        Long attemptId
) {
    public static QuizAttemptResponse from(QuizAttempt attempt) {
        return new QuizAttemptResponse(
                attempt.getQuestionId(),
                attempt.getSelectedOptionId(),
                attempt.isCorrect(),
                attempt.getSubmittedAt(),
                attempt.getId()
        );
    }
}