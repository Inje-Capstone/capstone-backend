package com.injecapstone.capstonebackend.quiz.service;

import com.injecapstone.capstonebackend.ai.client.AiClient;
import com.injecapstone.capstonebackend.ai.dto.AiOption;
import com.injecapstone.capstonebackend.ai.dto.AiQuizQuestion;
import com.injecapstone.capstonebackend.ai.dto.AiQuizQuestionDetail;
import com.injecapstone.capstonebackend.ai.dto.AiTodayRules;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.quiz.domain.QuizAttempt;
import com.injecapstone.capstonebackend.quiz.domain.ViewedTerm;
import com.injecapstone.capstonebackend.quiz.dto.QuizAnswerResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptSummaryResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse.Option;
import com.injecapstone.capstonebackend.quiz.dto.ViewedRulesRequest;
import com.injecapstone.capstonebackend.quiz.dto.ViewedRulesResponse;
import com.injecapstone.capstonebackend.quiz.repository.QuizAttemptRepository;
import com.injecapstone.capstonebackend.quiz.repository.ViewedTermRepository;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptDetailResponse;
import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LearningQuizService {

    private static final int ATTEMPT_COUNT = 10;

    // AI 문제은행에서 하루에 내는 문항 수
    private static final int DAILY_QUESTION_COUNT = 5;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // AI 용어 키 모양 (snake_case) — DB 컬럼 길이 64
    private static final Pattern TERM_ID = Pattern.compile("^[a-z0-9_]{1,64}$");

    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AiClient aiClient;
    private final ViewedTermRepository viewedTermRepository;

    private static final List<QuizQuestionResponse> QUESTIONS = List.of(
            new QuizQuestionResponse(
                    1,
                    "주자가 안타나 상대 팀의 실책 없이 다음 베이스로 뛰어가 안전하게 도달하는 플레이는?",
                    List.of(
                            new Option(1, "도루"),
                            new Option(2, "병살"),
                            new Option(3, "희생번트")
                    )
            )
    );

    private static final Map<Integer, Integer> CORRECT_OPTION_IDS =
            Map.of(1, 1);

    private static final Map<Integer, String> EXPLANATIONS = Map.of(
            1,
            "정답은 도루예요. 주자가 안타나 상대 수비의 실책 없이 "
                    + "다음 베이스로 뛰어가 안전하게 도달하는 플레이를 말해요."
    );

    // '오늘 본 룰 퀴즈': 그날 시청 기록의 용어로만 AI 고정 문제은행(1001번부터)에서 낸다.
    // 본 용어가 없으면 빈 목록(퀴즈 없음). AI 주소가 없는 로컬 개발 환경은 임시 문항 그대로.
    public List<QuizQuestionResponse> getQuestions(Long userId) {
        if (!aiClient.enabled()) {
            return QUESTIONS;
        }

        LocalDate today = LocalDate.now(SEOUL);
        List<String> termIds = viewedTermRepository.findTermIds(userId, today);

        if (termIds.isEmpty()) {
            return List.of();
        }

        String seed = userId + "-" + today;

        return aiClient.quizQuestions(DAILY_QUESTION_COUNT, seed, termIds).stream()
                .map(LearningQuizService::toResponse)
                .toList();
    }

    // 시청 기록: 이 경기를 t초까지 봤다 → AI가 그 구간 카드로 보여 준 용어를 오늘 본 용어에 더한다.
    // AI 호출(HTTP)이 있어 메서드 트랜잭션을 두지 않는다 — 용어마다 save()의 트랜잭션.
    public ViewedRulesResponse recordViewed(Long userId, ViewedRulesRequest request) {
        if (!aiClient.enabled()) {
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        // 카드 선택은 학습 수준에 따라 달라진다 — 진단 전이면 입문
        String level = user.getLearningLevel() == null
                ? LearningLevel.INTRODUCTORY.name()
                : user.getLearningLevel().name();

        AiTodayRules rules = aiClient.todayRules(request.gameId(), request.t(), level)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_INPUT)
                );

        LocalDate today = LocalDate.now(SEOUL);
        Set<String> seen = new HashSet<>(viewedTermRepository.findTermIds(userId, today));

        for (String termId : rules.termIds()) {
            if (termId == null || !TERM_ID.matcher(termId).matches() || !seen.add(termId)) {
                continue;
            }
            try {
                viewedTermRepository.save(
                        ViewedTerm.create(user, termId, request.gameId(), today)
                );
            } catch (DataIntegrityViolationException exception) {
                // 같은 요청이 동시에 들어와 이미 저장된 용어 — 건너뛴다
            }
        }

        return new ViewedRulesResponse(today, viewedTermRepository.findTermIds(userId, today));
    }

    // 오늘 본 용어 (시청 종료 팝업·퀴즈 안내용)
    public ViewedRulesResponse getViewedRules(Long userId) {
        LocalDate today = LocalDate.now(SEOUL);
        return new ViewedRulesResponse(today, viewedTermRepository.findTermIds(userId, today));
    }

    // AI 문항 조회(HTTP)를 DB 트랜잭션 밖에서 하려고 메서드 트랜잭션을 두지 않는다.
    // 쓰기는 풀이 기록 저장 한 번뿐이라 save()의 트랜잭션으로 충분하다.
    public QuizAnswerResponse grade(
            Long userId,
            int questionId,
            int optionId
    ) {
        GradedQuestion question = findQuestion(questionId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_INPUT)
                );

        boolean validOption = question.options().stream()
                .anyMatch(option -> option.optionId() == optionId);

        if (!validOption) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        int correctOptionId = question.correctOptionId();
        boolean correct = optionId == correctOptionId;
        String explanation = question.explanation();

        QuizAttempt savedAttempt = quizAttemptRepository.save(
                QuizAttempt.create(user, questionId, optionId, correct)
        );

        return new QuizAnswerResponse(
                questionId,
                correct,
                correctOptionId,
                explanation,
                savedAttempt.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getMyAttempts(
            Long userId,
            Boolean correct,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateDate(startDate);
        validateDate(endDate);

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        LocalDateTime startAt =
                startDate == null ? null : startDate.atStartOfDay();

        // 기존 submitted_at 컬럼의 datetime(6) 정밀도에 맞춰 종료일 전체를 포함합니다.
        LocalDateTime endAt = endDate == null
                ? null
                : endDate.atTime(LocalTime.of(23, 59, 59, 999_999_000));

        return quizAttemptRepository.findMyAttempts(
                        userId,
                        correct,
                        startAt,
                        endAt,
                        PageRequest.of(0, ATTEMPT_COUNT)
                )
                .stream()
                .map(QuizAttemptResponse::from)
                .toList();
    }

    private void validateDate(LocalDate date) {
        if (date != null && (date.getYear() < 1000 || date.getYear() > 9999)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    @Transactional(readOnly = true)
    public QuizAttemptSummaryResponse getMyAttemptSummary(Long userId) {
        long correctCount = 0;
        long incorrectCount = 0;

        for (var count : quizAttemptRepository.countByCorrectForUser(userId)) {
            if (count.getCorrect()) {
                correctCount = count.getAttemptCount();
            } else {
                incorrectCount = count.getAttemptCount();
            }
        }

        long totalCount = correctCount + incorrectCount;

        double accuracyRate = totalCount == 0
                ? 0.0
                : Math.round(correctCount * 1000.0 / totalCount) / 10.0;

        return new QuizAttemptSummaryResponse(
                totalCount,
                correctCount,
                incorrectCount,
                accuracyRate
        );
    }

    // AI 문항 조회(HTTP)가 끼어 있어 메서드 트랜잭션을 두지 않는다 (조회는 리포지토리 트랜잭션).
    public Optional<QuizAttemptDetailResponse> getMyAttempt(
            Long userId,
            Long attemptId
    ) {
        return quizAttemptRepository.findByIdAndUser_Id(attemptId, userId)
                .map(attempt -> {
                    GradedQuestion question = findQuestion(attempt.getQuestionId())
                            .orElseThrow(() -> missingQuestion());

                    return new QuizAttemptDetailResponse(
                            attempt.getQuestionId(),
                            attempt.getSelectedOptionId(),
                            attempt.isCorrect(),
                            attempt.getSubmittedAt(),
                            attempt.getId(),
                            question.text(),
                            question.options(),
                            question.correctOptionId(),
                            question.explanation()
                    );
                });
    }

    // AI 문제은행 먼저, 없으면 임시 문항 — AI로 바꾸기 전 풀이 기록(임시 문항 1번)도 계속 보이게
    private Optional<GradedQuestion> findQuestion(int questionId) {
        if (aiClient.enabled()) {
            Optional<AiQuizQuestionDetail> detail = aiClient.quizQuestion(questionId);

            if (detail.isPresent()) {
                AiQuizQuestionDetail item = detail.get();
                return Optional.of(new GradedQuestion(
                        item.text(),
                        toOptions(item.options()),
                        item.correctOptionId(),
                        item.explanation()
                ));
            }
        }

        return QUESTIONS.stream()
                .filter(item -> item.questionId() == questionId)
                .findFirst()
                .map(item -> new GradedQuestion(
                        item.text(),
                        item.options(),
                        CORRECT_OPTION_IDS.get(questionId),
                        EXPLANATIONS.get(questionId)
                ));
    }

    // AI 문항 기록인데 AI가 꺼져 있으면 503, 켜져 있는데 없으면 데이터 불일치
    private RuntimeException missingQuestion() {
        if (!aiClient.enabled()) {
            return new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
        return new IllegalStateException("풀이 기록에 해당하는 퀴즈 문제를 찾을 수 없습니다.");
    }

    private static QuizQuestionResponse toResponse(AiQuizQuestion question) {
        return new QuizQuestionResponse(
                question.questionId(),
                question.text(),
                toOptions(question.options())
        );
    }

    private static List<Option> toOptions(
            List<AiOption> options
    ) {
        return options.stream()
                .map(option -> new Option(option.optionId(), option.text()))
                .toList();
    }

    // 채점에 필요한 문항 정보 (정답 포함 — 서버 안에서만 쓴다)
    private record GradedQuestion(
            String text,
            List<Option> options,
            int correctOptionId,
            String explanation
    ) {
    }
}