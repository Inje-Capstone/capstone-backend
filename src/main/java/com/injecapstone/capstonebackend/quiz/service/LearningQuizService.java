package com.injecapstone.capstonebackend.quiz.service;

import com.injecapstone.capstonebackend.ai.client.AiClient;
import com.injecapstone.capstonebackend.ai.dto.AiOption;
import com.injecapstone.capstonebackend.ai.dto.AiQuizQuestion;
import com.injecapstone.capstonebackend.ai.dto.AiQuizQuestionDetail;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.quiz.domain.QuizAttempt;
import com.injecapstone.capstonebackend.quiz.dto.QuizAnswerResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptSummaryResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse.Option;
import com.injecapstone.capstonebackend.quiz.repository.QuizAttemptRepository;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptDetailResponse;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LearningQuizService {

    private static final int ATTEMPT_COUNT = 10;

    // AI 문제은행에서 하루에 내는 문항 수
    private static final int DAILY_QUESTION_COUNT = 5;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AiClient aiClient;

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

    // AI 주소가 있으면 AI 고정 문제은행(1001번부터)에서 사용자·날짜별로 같은 문항을, 없으면 임시 문항을 낸다.
    public List<QuizQuestionResponse> getQuestions(Long userId) {
        if (!aiClient.enabled()) {
            return QUESTIONS;
        }

        String seed = userId + "-" + LocalDate.now(SEOUL);

        return aiClient.quizQuestions(DAILY_QUESTION_COUNT, seed).stream()
                .map(LearningQuizService::toResponse)
                .toList();
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

        return new QuizAttemptSummaryResponse(
                correctCount + incorrectCount,
                correctCount,
                incorrectCount
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