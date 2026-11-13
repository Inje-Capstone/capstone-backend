package com.injecapstone.capstonebackend.quiz.controller;

import com.injecapstone.capstonebackend.quiz.dto.QuizAnswerRequest;
import com.injecapstone.capstonebackend.quiz.dto.QuizAnswerResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptSummaryResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse;
import com.injecapstone.capstonebackend.quiz.service.LearningQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
                learningQuizService.grade(userId, questionId, request.optionId())
        );
    }

    @Operation(
            summary = "내 퀴즈 풀이 기록 조회",
            description = """
                    조건에 맞는 본인의 풀이 기록을 최신순으로 10건 조회합니다.
                    correct로 정답 또는 오답을 선택할 수 있습니다.
                    startDate와 endDate는 yyyy-MM-dd 형식이며 각각 생략할 수 있습니다.
                    시작일과 종료일 모두 포함하며, 저장된 submittedAt 날짜를 기준으로 조회합니다.
                    조건을 생략하면 전체 기간의 최근 기록을 반환합니다.
                    """
    )
    @GetMapping("/attempts")
    public ResponseEntity<List<QuizAttemptResponse>> getMyAttempts(
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "correct", required = false) Boolean correct,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                learningQuizService.getMyAttempts(userId, correct, startDate, endDate)
        );
    }

    @Operation(summary = "내 퀴즈 풀이 요약 조회")
    @GetMapping("/attempts/summary")
    public ResponseEntity<QuizAttemptSummaryResponse> getMyAttemptSummary(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(learningQuizService.getMyAttemptSummary(userId));
    }

    @Operation(
            summary = "내 퀴즈 풀이 기록 상세 조회",
            description = "본인의 풀이 기록 한 건을 조회합니다. 조회할 수 없는 기록은 404를 반환합니다."
    )
    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<QuizAttemptResponse> getMyAttempt(
            @AuthenticationPrincipal Long userId,
            @PathVariable("attemptId") Long attemptId
    ) {
        return ResponseEntity.of(learningQuizService.getMyAttempt(userId, attemptId));
    }
}