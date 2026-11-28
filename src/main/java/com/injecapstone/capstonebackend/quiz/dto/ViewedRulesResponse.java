package com.injecapstone.capstonebackend.quiz.dto;

import java.time.LocalDate;
import java.util.List;

// 그날 본 용어 키 전체 (여러 경기 합산, 처음 본 순)
public record ViewedRulesResponse(
        LocalDate date,
        List<String> termIds
) {
}
