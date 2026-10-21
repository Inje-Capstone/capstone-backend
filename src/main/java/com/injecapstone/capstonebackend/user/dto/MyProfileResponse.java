package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import com.injecapstone.capstonebackend.user.domain.SupportedTeam;
import com.injecapstone.capstonebackend.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인한 사용자의 기본 정보와 학습 설정")
public record MyProfileResponse(
        @Schema(description = "사용자 ID", example = "4")
        Long userId,

        @Schema(
                description = "이메일",
                example = "onboarding-test@example.com",
                nullable = true
        )
        String email,

        @Schema(description = "닉네임", example = "루키")
        String nickname,

        @Schema(
                description = "학습 수준 코드. 수준 미설정 시 null",
                example = "BEGINNER",
                nullable = true
        )
        LearningLevel learningLevel,

        @Schema(
                description = "학습 수준 표시 이름. 수준 미설정 시 null",
                example = "초보",
                nullable = true
        )
        String learningLevelDisplayName,

        @Schema(
                description = "응원팀 코드. 미선택 시 null, NONE은 '아직 없어요'를 선택한 상태",
                example = "NONE",
                nullable = true
        )
        SupportedTeam supportedTeam,

        @Schema(
                description = "응원팀 표시 이름. 미선택 시 null",
                example = "아직 없어요",
                nullable = true
        )
        String supportedTeamDisplayName,

        @Schema(description = "경기 알림 수신 여부", example = "false")
        boolean gameNotificationEnabled,

        @Schema(description = "온보딩 완료 여부", example = "true")
        boolean onboardingCompleted
) {

    // 사용자 정보와 선택값의 표시 이름을 내 정보 응답으로 변환
    public static MyProfileResponse from(User user) {
        LearningLevel learningLevel = user.getLearningLevel();
        SupportedTeam supportedTeam = user.getSupportedTeam();

        return new MyProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                learningLevel,
                learningLevel != null ? learningLevel.getDisplayName() : null,
                supportedTeam,
                supportedTeam != null ? supportedTeam.getDisplayName() : null,
                Boolean.TRUE.equals(user.getGameNotificationEnabled()),
                user.isOnboardingCompleted()
        );
    }
}