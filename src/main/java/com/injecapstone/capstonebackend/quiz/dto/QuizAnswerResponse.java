package com.injecapstone.capstonebackend.quiz.dto;

public record QuizAnswerResponse(
        int questionId,
        boolean correct
) {
}