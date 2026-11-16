package com.injecapstone.capstonebackend.quiz.dto;

import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse.Option;

import java.time.LocalDateTime;
import java.util.List;

public record QuizAttemptDetailResponse(
        int questionId,
        int selectedOptionId,
        boolean correct,
        LocalDateTime submittedAt,
        Long attemptId,
        String questionText,
        List<Option> options,
        int correctOptionId,
        String explanation
) {
}