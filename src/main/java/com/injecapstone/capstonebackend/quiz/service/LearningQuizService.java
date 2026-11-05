package com.injecapstone.capstonebackend.quiz.service;

import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.quiz.domain.QuizAttempt;
import com.injecapstone.capstonebackend.quiz.dto.QuizAnswerResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse.Option;
import com.injecapstone.capstonebackend.quiz.repository.QuizAttemptRepository;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LearningQuizService {

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

        boolean correct =
                Integer.valueOf(optionId).equals(
                        CORRECT_OPTION_IDS.get(questionId)
                );

        quizAttemptRepository.save(
                QuizAttempt.create(user, questionId, optionId, correct)
        );

        return new QuizAnswerResponse(questionId, correct);
    }
}