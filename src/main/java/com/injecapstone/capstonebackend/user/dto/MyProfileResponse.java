package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import com.injecapstone.capstonebackend.user.domain.SupportedTeam;
import com.injecapstone.capstonebackend.user.domain.User;

public record MyProfileResponse(
        Long userId,
        String email,
        String nickname,
        LearningLevel learningLevel,
        String learningLevelDisplayName,
        SupportedTeam supportedTeam,
        String supportedTeamDisplayName,
        boolean gameNotificationEnabled,
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