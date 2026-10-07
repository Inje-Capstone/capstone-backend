package com.injecapstone.capstonebackend.user.service;

import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import com.injecapstone.capstonebackend.user.dto.DiagnosisQuestionResponse;
import com.injecapstone.capstonebackend.user.dto.DiagnosisResultResponse;
import com.injecapstone.capstonebackend.user.dto.DiagnosisSubmitRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DiagnosisQuiz {

    // 개발용 임시 문항
    private static final List<Question> QUESTIONS = List.of(
            new Question(
                    1,
                    "한 팀이 한 이닝의 공격을 마치는 아웃 개수는?",
                    List.of("2개", "3개", "4개"),
                    2
            ),
            new Question(
                    2,
                    "타자가 스트라이크 3개로 아웃되는 것을 무엇이라 할까요?",
                    List.of("볼넷", "홈런", "삼진"),
                    3
            ),
            new Question(
                    3,
                    "타자가 볼 4개를 얻어 1루로 나가는 것을 무엇이라 할까요?",
                    List.of("볼넷", "삼진", "도루"),
                    1
            )
    );

    public List<DiagnosisQuestionResponse> getQuestions() {
        return QUESTIONS.stream()
                .map(question -> new DiagnosisQuestionResponse(
                        question.id(),
                        question.text(),
                        List.of(
                                new DiagnosisQuestionResponse.Option(
                                        1, question.options().get(0)
                                ),
                                new DiagnosisQuestionResponse.Option(
                                        2, question.options().get(1)
                                ),
                                new DiagnosisQuestionResponse.Option(
                                        3, question.options().get(2)
                                )
                        )
                ))
                .toList();
    }

    public DiagnosisResultResponse grade(
            DiagnosisSubmitRequest request
    ) {
        if (request.answers() == null
                || request.answers().size() != QUESTIONS.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Map<Integer, Integer> answers = new HashMap<>();

        for (DiagnosisSubmitRequest.Answer answer : request.answers()) {
            if (answer == null
                    || answer.questionId() == null
                    || answer.optionId() == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            boolean knownQuestion = QUESTIONS.stream()
                    .anyMatch(question ->
                            question.id() == answer.questionId()
                    );

            if (!knownQuestion
                    || answer.optionId() < 1
                    || answer.optionId() > 3) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            // 같은 문항을 여러 번 제출한 경우 거부
            if (answers.putIfAbsent(
                    answer.questionId(), answer.optionId()
            ) != null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
        }

        int correctCount = 0;

        for (Question question : QUESTIONS) {
            Integer selectedOption = answers.get(question.id());

            if (selectedOption == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            if (selectedOption == question.correctOptionId()) {
                correctCount++;
            }
        }

        return new DiagnosisResultResponse(
                correctCount,
                QUESTIONS.size(),
                determineLevel(correctCount)
        );
    }

    // 개발용 임시 수준 판정 기준
    private LearningLevel determineLevel(int correctCount) {
        return switch (correctCount) {
            case 3 -> LearningLevel.FAMILIAR;
            case 2 -> LearningLevel.BEGINNER;
            default -> LearningLevel.INTRODUCTORY;
        };
    }

    // 정답을 포함하는 서버 내부 데이터
    private record Question(
            int id,
            String text,
            List<String> options,
            int correctOptionId
    ) {
    }
}