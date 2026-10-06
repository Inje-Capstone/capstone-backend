package com.injecapstone.capstonebackend.user.controller;

import com.injecapstone.capstonebackend.user.dto.OnboardingStatusResponse;
import com.injecapstone.capstonebackend.user.dto.TermsAgreementRequest;
import com.injecapstone.capstonebackend.user.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.injecapstone.capstonebackend.user.dto.DiagnosisQuestionResponse;
import com.injecapstone.capstonebackend.user.dto.DiagnosisResultResponse;
import com.injecapstone.capstonebackend.user.dto.DiagnosisSubmitRequest;
import com.injecapstone.capstonebackend.user.dto.SupportedTeamRequest;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "내 온보딩 상태 조회")
    @GetMapping
    public ResponseEntity<OnboardingStatusResponse> getStatus(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(
                onboardingService.getStatus(userId)
        );
    }

    @Operation(summary = "온보딩 약관 동의 저장")
    @PutMapping("/terms")
    public ResponseEntity<OnboardingStatusResponse> agreeToTerms(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TermsAgreementRequest request
    ) {
        return ResponseEntity.ok(
                onboardingService.agreeToTerms(userId, request)
        );
    }
    @Operation(summary = "온보딩 수준 진단 문항 조회")
    @GetMapping("/diagnosis/questions")
    public ResponseEntity<List<DiagnosisQuestionResponse>> getDiagnosisQuestions(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(
                onboardingService.getDiagnosisQuestions(userId)
        );
    }

    @Operation(summary = "온보딩 수준 진단 답안 제출")
    @PostMapping("/diagnosis")
    public ResponseEntity<DiagnosisResultResponse> submitDiagnosis(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DiagnosisSubmitRequest request
    ) {
        return ResponseEntity.ok(
                onboardingService.submitDiagnosis(userId, request)
        );
    }

    // 응원팀 또는 '아직 없어요' 선택
    @Operation(summary = "온보딩 응원팀 선택")
    @PutMapping("/team")
    public ResponseEntity<OnboardingStatusResponse> selectSupportedTeam(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SupportedTeamRequest request
    ) {
        return ResponseEntity.ok(
                onboardingService.selectSupportedTeam(userId, request)
        );
    }

    // 모든 필수 단계를 확인하고 온보딩 완료
    @Operation(summary = "온보딩 완료")
    @PostMapping("/complete")
    public ResponseEntity<OnboardingStatusResponse> completeOnboarding(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(
                onboardingService.completeOnboarding(userId)
        );
    }
}