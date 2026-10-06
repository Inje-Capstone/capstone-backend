package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import com.injecapstone.capstonebackend.user.domain.SupportedTeam;
import com.injecapstone.capstonebackend.user.domain.User;

public record OnboardingStatusResponse(
        boolean requiredTermsAgreed,
        boolean gameNotificationEnabled,
        boolean diagnosisCompleted,
        LearningLevel learningLevel,
        Integer diagnosisScore,
        boolean supportedTeamSelected,
        SupportedTeam supportedTeam,
        boolean onboardingCompleted
) {

    public static OnboardingStatusResponse from(User user) {
        return new OnboardingStatusResponse(
                user.hasAgreedToRequiredTerms(),
                Boolean.TRUE.equals(user.getGameNotificationEnabled()),
                user.hasCompletedDiagnosis(),
                user.getLearningLevel(),
                user.getDiagnosisScore(),
                user.hasSelectedSupportedTeam(),
                user.getSupportedTeam(),
                user.isOnboardingCompleted()
        );
    }
}