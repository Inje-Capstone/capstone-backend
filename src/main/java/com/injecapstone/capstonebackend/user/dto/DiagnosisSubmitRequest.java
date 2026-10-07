package com.injecapstone.capstonebackend.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DiagnosisSubmitRequest(

        @NotNull
        @Size(min = 3, max = 3, message = "3문항에 모두 답해주세요.")
        List<@NotNull @Valid Answer> answers

) {

    public record Answer(

            @NotNull
            @Min(1)
            @Max(3)
            Integer questionId,

            @NotNull
            @Min(1)
            @Max(3)
            Integer optionId

    ) {
    }
}