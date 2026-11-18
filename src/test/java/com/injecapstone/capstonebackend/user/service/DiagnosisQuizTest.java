package com.injecapstone.capstonebackend.user.service;

import com.injecapstone.capstonebackend.ai.client.AiClient;
import com.injecapstone.capstonebackend.ai.dto.AiDiagnosisQuestion;
import com.injecapstone.capstonebackend.ai.dto.AiDiagnosisResult;
import com.injecapstone.capstonebackend.ai.dto.AiOption;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import com.injecapstone.capstonebackend.user.dto.DiagnosisQuestionResponse;
import com.injecapstone.capstonebackend.user.dto.DiagnosisResultResponse;
import com.injecapstone.capstonebackend.user.dto.DiagnosisSubmitRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiagnosisQuizTest {

    private static DiagnosisSubmitRequest request(int... questionAndOption) {
        List<DiagnosisSubmitRequest.Answer> answers = new java.util.ArrayList<>();
        for (int i = 0; i < questionAndOption.length; i += 2) {
            answers.add(new DiagnosisSubmitRequest.Answer(
                    questionAndOption[i], questionAndOption[i + 1]
            ));
        }
        return new DiagnosisSubmitRequest(answers);
    }

    private static void assertError(Runnable call, ErrorCode code) {
        assertThatThrownBy(call::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(code);
    }

    @Nested
    class AI_주소가_없으면_임시_문항 {

        private final DiagnosisQuiz quiz = new DiagnosisQuiz(AiClient.disabled());

        @Test
        void 임시_3문항과_보기_3개() {
            List<DiagnosisQuestionResponse> questions = quiz.getQuestions();

            assertThat(questions).hasSize(3);
            assertThat(questions.get(0).options()).hasSize(3);
        }

        @Test
        void 모두_맞히면_익숙() {
            DiagnosisResultResponse result = quiz.grade(request(1, 2, 2, 3, 3, 1));

            assertThat(result.correctCount()).isEqualTo(3);
            assertThat(result.learningLevel()).isEqualTo(LearningLevel.FAMILIAR);
        }

        @Test
        void 빠진_문항_중복_범위_밖_보기는_거부() {
            assertError(() -> quiz.grade(request(1, 2, 2, 3)), ErrorCode.INVALID_INPUT);
            assertError(() -> quiz.grade(request(1, 2, 1, 3, 3, 1)), ErrorCode.INVALID_INPUT);
            assertError(() -> quiz.grade(request(1, 4, 2, 3, 3, 1)), ErrorCode.INVALID_INPUT);
        }
    }

    @Nested
    class AI_주소가_있으면_AI_문제은행 {

        private final AiClient ai = mock(AiClient.class);
        private final DiagnosisQuiz quiz = new DiagnosisQuiz(ai);

        AI_주소가_있으면_AI_문제은행() {
            when(ai.enabled()).thenReturn(true);
        }

        private AiDiagnosisResult result(String level, Integer... chosen) {
            List<AiDiagnosisResult.QuestionResult> results = new java.util.ArrayList<>();
            for (int i = 0; i < chosen.length; i++) {
                results.add(new AiDiagnosisResult.QuestionResult(i + 1, chosen[i], true));
            }
            return new AiDiagnosisResult(2, chosen.length, level, results);
        }

        @Test
        void AI_문항을_백엔드_DTO로_옮긴다() {
            when(ai.diagnosisQuestions()).thenReturn(List.of(new AiDiagnosisQuestion(
                    1, "walk", "볼 4개면?",
                    List.of(new AiOption(1, "아웃"), new AiOption(2, "1루"),
                            new AiOption(3, "2루"), new AiOption(4, "그대로"))
            )));

            List<DiagnosisQuestionResponse> questions = quiz.getQuestions();

            assertThat(questions).hasSize(1);
            assertThat(questions.get(0).question()).isEqualTo("볼 4개면?");
            assertThat(questions.get(0).options()).extracting("optionId")
                    .containsExactly(1, 2, 3, 4);
        }

        @Test
        void AI가_매긴_수준을_그대로_쓴다_보기_4번도_받는다() {
            when(ai.gradeDiagnosis(anyList())).thenReturn(result("BEGINNER", 4, 1, 2));

            DiagnosisResultResponse graded = quiz.grade(request(1, 4, 2, 1, 3, 2));

            assertThat(graded.learningLevel()).isEqualTo(LearningLevel.BEGINNER);
            assertThat(graded.correctCount()).isEqualTo(2);
            assertThat(graded.totalCount()).isEqualTo(3);
        }

        @Test
        void 답하지_않은_문항이_있으면_거부() {
            when(ai.gradeDiagnosis(anyList())).thenReturn(result("INTRODUCTORY", 1, 2, null));

            assertError(() -> quiz.grade(request(1, 1, 2, 2, 9, 1)), ErrorCode.INVALID_INPUT);
        }

        @Test
        void 문항_수보다_적게_답하면_거부() {
            when(ai.gradeDiagnosis(anyList())).thenReturn(new AiDiagnosisResult(
                    1, 3, "INTRODUCTORY",
                    List.of(new AiDiagnosisResult.QuestionResult(1, 1, true),
                            new AiDiagnosisResult.QuestionResult(2, null, false),
                            new AiDiagnosisResult.QuestionResult(3, null, false))
            ));

            assertError(() -> quiz.grade(request(1, 1)), ErrorCode.INVALID_INPUT);
        }

        @Test
        void 모르는_수준_이름은_503() {
            when(ai.gradeDiagnosis(anyList())).thenReturn(result("EXPERT", 1, 1, 1));

            assertError(() -> quiz.grade(request(1, 1, 2, 1, 3, 1)),
                    ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        @Test
        void 중복_제출은_AI를_부르기_전에_거부() {
            assertError(() -> quiz.grade(request(1, 1, 1, 2)), ErrorCode.INVALID_INPUT);
            verify(ai, never()).gradeDiagnosis(anyList());
        }
    }
}
