package com.injecapstone.capstonebackend.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, length = 255)
    private String email;

    @Column(nullable = true, length = 255)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 서비스 이용약관 동의 시각
    private LocalDateTime termsAgreedAt;

    // 개인정보 수집·이용 동의 시각
    private LocalDateTime privacyAgreedAt;

    // 선택 항목: 새 경기 알림 받기
    private Boolean gameNotificationEnabled = false;

    // 온보딩 완료 시각
    private LocalDateTime onboardingCompletedAt;

    //유저 수준 진단
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LearningLevel learningLevel;

    private Integer diagnosisScore;

    private LocalDateTime diagnosisCompletedAt;

    public void saveDiagnosis(
            LearningLevel learningLevel,
            int diagnosisScore
    ) {
        this.learningLevel = learningLevel;
        this.diagnosisScore = diagnosisScore;
        this.diagnosisCompletedAt = LocalDateTime.now();
    }

    //응원팀 선택
    // null: 아직 선택하지 않음 / NONE: '아직 없어요' 선택
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SupportedTeam supportedTeam;
    // 응원팀 선택값 저장
    public void selectSupportedTeam(SupportedTeam supportedTeam) {
        this.supportedTeam = supportedTeam;
    }

    // 수준 진단 완료 여부
    public boolean hasCompletedDiagnosis() {
        return learningLevel != null
                && diagnosisScore != null
                && diagnosisCompletedAt != null;
    }

    // 응원팀 선택 단계 완료 여부
    // NONE도 사용자가 직접 선택한 값이므로 완료로 처리
    public boolean hasSelectedSupportedTeam() {
        return supportedTeam != null;
    }

    // 온보딩 완료 처리
    // 재요청해도 최초 완료 시각 유지
    public void completeOnboarding() {
        if (this.onboardingCompletedAt == null) {
            this.onboardingCompletedAt = LocalDateTime.now();
        }
    }

    // 사용자의 닉네임 변경
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    // 경기 알림 수신 여부만 변경
    public void changeGameNotificationEnabled(boolean gameNotificationEnabled) {
        this.gameNotificationEnabled = gameNotificationEnabled;
    }

    //User 정보
    protected User(
            String email,
            String password,
            String nickname,
            UserRole role
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public static User create(
            String email,
            String password,
            String nickname
    ) {
        return new User(
                email,
                password,
                nickname,
                UserRole.USER
        );
    }

    public static User createSocial(
            String email,
            String nickname
    ) {
        return new User(
                email,
                null,
                nickname,
                UserRole.USER
        );
    }

    public void agreeToTerms(boolean gameNotificationEnabled) {
        LocalDateTime now = LocalDateTime.now();

        // 다시 요청해도 최초 동의 시각은 유지
        if (this.termsAgreedAt == null) {
            this.termsAgreedAt = now;
        }

        if (this.privacyAgreedAt == null) {
            this.privacyAgreedAt = now;
        }

        this.gameNotificationEnabled = gameNotificationEnabled;
    }

    public boolean hasAgreedToRequiredTerms() {
        return termsAgreedAt != null && privacyAgreedAt != null;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompletedAt != null;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();

    }
}