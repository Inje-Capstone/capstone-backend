package com.injecapstone.capstonebackend.quiz.controller;

import com.injecapstone.capstonebackend.quiz.dto.*;
import com.injecapstone.capstonebackend.quiz.service.LearningQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class LearningQuizController {

    private final LearningQuizService learningQuizService;

    @Operation(
            summary = "학습용 퀴즈 문제 조회",
            description = "학습용 퀴즈의 문제와 선택지를 조회합니다. 정답은 반환하지 않습니다."
    )
    @GetMapping("/questions")
    public ResponseEntity<List<QuizQuestionResponse>> getQuestions() {
        return ResponseEntity.ok(learningQuizService.getQuestions());
    }
    @Operation(
            summary = "학습용 퀴즈 답안 채점 및 기록 저장",
            description = "답안을 채점하고 로그인한 사용자의 풀이 기록을 저장합니다."
    )
    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<QuizAnswerResponse> grade(
            @AuthenticationPrincipal Long userId,
            @PathVariable("questionId") int questionId,
            @Valid @RequestBody QuizAnswerRequest request
    ) {
        return ResponseEntity.ok(
                learningQuizService.grade(
                        userId,
                        questionId,
                        request.optionId()
                )
        );
    }
    @Operation(
            summary = "내 퀴즈 풀이 기록 조회",
            description = "본인의 최근 퀴즈 풀이 기록을 최대 20건 조회합니다. correct로 정답 또는 오답만 조회할 수 있습니다."
    )
    @GetMapping("/attempts")
    public ResponseEntity<List<QuizAttemptResponse>> getMyAttempts(
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "correct", required = false) Boolean correct
    ) {
        return ResponseEntity.ok(learningQuizService.getMyAttempts(userId, correct));
    }

    @Operation(summary = "내 퀴즈 풀이 요약 조회")
    @GetMapping("/attempts/summary")
    public ResponseEntity<QuizAttemptSummaryResponse> getMyAttemptSummary(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(learningQuizService.getMyAttemptSummary(userId));
    }
}