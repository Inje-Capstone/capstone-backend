package com.injecapstone.capstonebackend.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// 시청 기록 — 이 경기를 영상 몇 초까지 봤는지
public record ViewedRulesRequest(

        @NotNull
        @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$")
        String gameId,

        // 영상 타임코드(초). 하루 경기 영상은 길어야 몇 시간이다
        @NotNull
        @Min(0)
        @Max(86_400)
        Integer t

) {
}
