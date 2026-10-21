package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학습 수준 선택 목록의 항목")
public record LearningLevelResponse(
        @Schema(
                description = "학습 수준 코드. 학습 수준 수정 요청의 learningLevel 값으로 사용",
                example = "BEGINNER"
        )
        LearningLevel code,

        @Schema(
                description = "화면에 표시할 학습 수준 이름",
                example = "초보"
        )
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