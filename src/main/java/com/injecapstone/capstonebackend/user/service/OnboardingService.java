package com.injecapstone.capstonebackend.user.service;

import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.dto.OnboardingStatusResponse;
import com.injecapstone.capstonebackend.user.dto.TermsAgreementRequest;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.injecapstone.capstonebackend.user.dto.DiagnosisQuestionResponse;
import com.injecapstone.capstonebackend.user.dto.DiagnosisResultResponse;
import com.injecapstone.capstonebackend.user.dto.DiagnosisSubmitRequest;
import com.injecapstone.capstonebackend.user.dto.SupportedTeamRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingService {

    private final UserRepository userRepository;
    private final DiagnosisQuiz diagnosisQuiz;

    public OnboardingStatusResponse getStatus(Long userId) {
        return OnboardingStatusResponse.from(findUser(userId));
    }

    // 수준 진단 퀴즈 문항 조회
// 필수 약관에 동의한 사용자에게 정답을 제외한 문항과 보기를 반환
    public List<DiagnosisQuestionResponse> getDiagnosisQuestions(
            Long userId
    ) {
        // 로그인한 사용자 조회
        User user = findUser(userId);

        // 필수 약관에 동의하지 않았다면 진행 불가
        if (!user.hasAgreedToRequiredTerms()) {
            throw new BusinessException(
                    ErrorCode.REQUIRED_TERMS_NOT_AGREED
            );
        }

        // 진단 퀴즈 문항과 보기 반환
        return diagnosisQuiz.getQuestions();
    }

    // 수준 진단 퀴즈 답안 제출 및 결과 저장
// 서버에서 답안을 채점하고 사용자의 학습 수준, 점수, 진단 완료 시각을 저장한다.
    @Transactional
    public DiagnosisResultResponse submitDiagnosis(
            Long userId,
            DiagnosisSubmitRequest request
    ) {
        // 로그인한 사용자 조회
        User user = findUser(userId);

        // 필수 약관에 동의하지 않았다면 진행 불가
        if (!user.hasAgreedToRequiredTerms()) {
            throw new BusinessException(
                    ErrorCode.REQUIRED_TERMS_NOT_AGREED
            );
        }

        // 답안 검증 및 채점 후 학습 수준 판정
        DiagnosisResultResponse result = diagnosisQuiz.grade(request);

        // 사용자 엔티티에 학습 수준, 점수, 진단 완료 시각 반영
        // 트랜잭션 종료 시 JPA가 변경 내용을 DB에 반영
        user.saveDiagnosis(
                result.learningLevel(),
                result.correctCount()
        );

        // 프론트엔드 진단 결과 화면에 표시할 결과 반환
        return result;
    }

    @Transactional
    public OnboardingStatusResponse agreeToTerms(
            Long userId,
            TermsAgreementRequest request
    ) {
        if (!Boolean.TRUE.equals(request.termsAgreed())
                || !Boolean.TRUE.equals(request.privacyAgreed())
                || request.gameNotificationEnabled() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        User user = findUser(userId);
        user.agreeToTerms(request.gameNotificationEnabled());

        // 트랜잭션 종료 시 JPA가 변경 내용을 DB에 반영
        return OnboardingStatusResponse.from(user);
    }

    // 응원팀 선택값 저장
// 필수 약관 동의와 수준 진단을 마친 사용자만 진행 가능
    @Transactional
    public OnboardingStatusResponse selectSupportedTeam(
            Long userId,
            SupportedTeamRequest request
    ) {
        User user = findUser(userId);

        if (!user.hasAgreedToRequiredTerms()) {
            throw new BusinessException(
                    ErrorCode.REQUIRED_TERMS_NOT_AGREED
            );
        }

        if (!user.hasCompletedDiagnosis()) {
            throw new BusinessException(
                    ErrorCode.DIAGNOSIS_NOT_COMPLETED
            );
        }

        if (request.supportedTeam() == null) {
            throw new BusinessException(
                    ErrorCode.SUPPORTED_TEAM_NOT_SELECTED
            );
        }

        // NONE도 유효한 선택값으로 저장
        user.selectSupportedTeam(request.supportedTeam());

        return OnboardingStatusResponse.from(user);
    }

    // 온보딩 완료 처리
// 프론트의 '시작하기' 버튼 클릭 시 호출
// 서버가 필수 단계 완료 여부를 확인한 뒤 완료 시각 저장
    @Transactional
    public OnboardingStatusResponse completeOnboarding(Long userId) {
        User user = findUser(userId);

        if (!user.hasAgreedToRequiredTerms()) {
            throw new BusinessException(
                    ErrorCode.REQUIRED_TERMS_NOT_AGREED
            );
        }

        if (!user.hasCompletedDiagnosis()) {
            throw new BusinessException(
                    ErrorCode.DIAGNOSIS_NOT_COMPLETED
            );
        }

        if (!user.hasSelectedSupportedTeam()) {
            throw new BusinessException(
                    ErrorCode.SUPPORTED_TEAM_NOT_SELECTED
            );
        }

        user.completeOnboarding();

        return OnboardingStatusResponse.from(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );
    }
}