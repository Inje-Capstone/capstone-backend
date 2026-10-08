package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import com.injecapstone.capstonebackend.user.domain.SupportedTeam;
import com.injecapstone.capstonebackend.user.domain.User;

public record MyProfileResponse(
        Long userId,
        String email,
        String nickname,
        LearningLevel learningLevel,
        SupportedTeam supportedTeam,
        boolean gameNotificationEnabled,
        boolean onboardingCompleted
) {

    // 사용자 정보를 내 정보 조회 응답으로 변환
    public static MyProfileResponse from(User user) {
        return new MyProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getLearningLevel(),
                user.getSupportedTeam(),
                Boolean.TRUE.equals(user.getGameNotificationEnabled()),
                user.isOnboardingCompleted()
        );
    }
}