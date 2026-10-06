package com.injecapstone.capstonebackend.user.dto;

import java.util.List;

public record DiagnosisQuestionResponse(
        int questionId,
        String question,
        List<Option> options
) {

    public record Option(
            int optionId,
            String text
    ) {
    }
}