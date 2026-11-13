package com.injecapstone.capstonebackend.quiz.service;

import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.quiz.domain.QuizAttempt;
import com.injecapstone.capstonebackend.quiz.dto.QuizAnswerResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptSummaryResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse.Option;
import com.injecapstone.capstonebackend.quiz.repository.QuizAttemptRepository;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LearningQuizService {

    private static final int ATTEMPT_COUNT = 10;

    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;

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

    public List<QuizQuestionResponse> getQuestions() {
        return QUESTIONS;
    }

    @Transactional
    public QuizAnswerResponse grade(
            Long userId,
            int questionId,
            int optionId
    ) {
        QuizQuestionResponse question = QUESTIONS.stream()
                .filter(item -> item.questionId() == questionId)
                .findFirst()
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

        boolean correct = Integer.valueOf(optionId)
                .equals(CORRECT_OPTION_IDS.get(questionId));

        quizAttemptRepository.save(
                QuizAttempt.create(user, questionId, optionId, correct)
        );

        return new QuizAnswerResponse(questionId, correct);
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

    @Transactional(readOnly = true)
    public Optional<QuizAttemptResponse> getMyAttempt(Long userId, Long attemptId) {
        return quizAttemptRepository.findByIdAndUser_Id(attemptId, userId)
                .map(QuizAttemptResponse::from);
    }
}