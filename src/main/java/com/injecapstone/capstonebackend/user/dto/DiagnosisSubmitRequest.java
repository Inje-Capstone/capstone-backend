package com.injecapstone.capstonebackend.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DiagnosisSubmitRequest(

        // 문항 수는 문제은행(AI 또는 임시 문항)이 정한다 — 모두 답했는지는 채점에서 확인
        @NotNull
        @Size(min = 1, max = 20, message = "모든 문항에 답해주세요.")
        List<@NotNull @Valid Answer> answers

) {

    public record Answer(

            @NotNull
            @Min(1)
            @Max(20)
            Integer questionId,

            // AI 문제은행은 보기 4개, 임시 문항은 3개
            @NotNull
            @Min(1)
            @Max(4)
            Integer optionId

    ) {
    }
}
