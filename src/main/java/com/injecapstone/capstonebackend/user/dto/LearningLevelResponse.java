package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.LearningLevel;

public record LearningLevelResponse(
        LearningLevel code,
        String displayName
) {

    // 학습 수준 enum을 선택 목록 응답으로 변환
    public static LearningLevelResponse from(LearningLevel level) {
        return new LearningLevelResponse(
                level,
                level.getDisplayName()
        );
    }
}