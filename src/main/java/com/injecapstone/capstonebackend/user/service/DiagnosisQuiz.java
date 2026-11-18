package com.injecapstone.capstonebackend.user.service;

import com.injecapstone.capstonebackend.ai.client.AiClient;
import com.injecapstone.capstonebackend.ai.dto.AiDiagnosisRequest;
import com.injecapstone.capstonebackend.ai.dto.AiDiagnosisResult;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import com.injecapstone.capstonebackend.user.dto.DiagnosisQuestionResponse;
import com.injecapstone.capstonebackend.user.dto.DiagnosisResultResponse;
import com.injecapstone.capstonebackend.user.dto.DiagnosisSubmitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 수준 진단 퀴즈.
 *
 * <p>AI 주소(ai.base-url)가 있으면 AI 문제은행으로 출제·채점하고, 없으면 개발용 임시 문항을 쓴다.
 * 한 환경에서는 둘 중 하나만 쓴다 — 문항 번호가 같아서 섞으면 엉뚱하게 채점된다.
 */
@Component
@RequiredArgsConstructor
public class DiagnosisQuiz {

    private final AiClient aiClient;

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
        if (aiClient.enabled()) {
            return aiClient.diagnosisQuestions().stream()
                    .map(question -> new DiagnosisQuestionResponse(
                            question.questionId(),
                            question.question(),
                            question.options().stream()
                                    .map(option -> new DiagnosisQuestionResponse.Option(
                                            option.optionId(), option.text()
                                    ))
                                    .toList()
                    ))
                    .toList();
        }

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
        Map<Integer, Integer> answers = collectAnswers(request);

        if (aiClient.enabled()) {
            return gradeWithAi(answers);
        }

        if (answers.size() != QUESTIONS.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        for (Map.Entry<Integer, Integer> answer : answers.entrySet()) {
            boolean knownQuestion = QUESTIONS.stream()
                    .anyMatch(question -> question.id() == answer.getKey());

            if (!knownQuestion || answer.getValue() < 1 || answer.getValue() > 3) {
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

    // 빈 답·같은 문항 중복 제출은 거부
    private Map<Integer, Integer> collectAnswers(DiagnosisSubmitRequest request) {
        if (request.answers() == null || request.answers().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Map<Integer, Integer> answers = new HashMap<>();

        for (DiagnosisSubmitRequest.Answer answer : request.answers()) {
            if (answer == null
                    || answer.questionId() == null
                    || answer.optionId() == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            if (answers.putIfAbsent(
                    answer.questionId(), answer.optionId()
            ) != null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
        }

        return answers;
    }

    // AI는 빠진 문항·모르는 번호를 틀림으로 센다 — 백엔드는 모든 문항에 답했는지 따로 확인한다.
    private DiagnosisResultResponse gradeWithAi(Map<Integer, Integer> answers) {
        AiDiagnosisResult result = aiClient.gradeDiagnosis(
                answers.entrySet().stream()
                        .map(answer -> new AiDiagnosisRequest.Answer(
                                answer.getKey(), answer.getValue()
                        ))
                        .toList()
        );

        boolean allAnswered = result.totalCount() == answers.size()
                && result.results().size() == result.totalCount()
                && result.results().stream()
                .allMatch(item -> item.chosenOptionId() != null);

        if (!allAnswered) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        LearningLevel level;

        try {
            level = LearningLevel.valueOf(result.learningLevel());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        return new DiagnosisResultResponse(
                result.correctCount(),
                result.totalCount(),
                level
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
