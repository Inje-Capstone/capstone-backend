package com.injecapstone.capstonebackend.quiz.dto;

import java.util.List;

public record QuizQuestionResponse(
        int questionId,
        String text,
        List<Option> options
) {
    public record Option(int optionId, String text) {
    }
}