package com.injecapstone.capstonebackend.quiz.controller;

import com.injecapstone.capstonebackend.quiz.dto.QuizAnswerRequest;
import com.injecapstone.capstonebackend.quiz.dto.QuizAnswerResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptSummaryResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse;
import com.injecapstone.capstonebackend.quiz.dto.ViewedRulesRequest;
import com.injecapstone.capstonebackend.quiz.dto.ViewedRulesResponse;
import com.injecapstone.capstonebackend.quiz.service.LearningQuizService;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptDetailResponse;
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
            description = """
                    오늘 본 용어(시청 기록)로만 최대 5문제를 냅니다. 정답은 반환하지 않습니다.
                    오늘 본 용어가 없으면 빈 목록입니다. 같은 날 같은 사용자는 같은 문제입니다.
                    """
    )
    @GetMapping("/questions")
    public ResponseEntity<List<QuizQuestionResponse>> getQuestions(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(learningQuizService.getQuestions(userId));
    }

    @Operation(
            summary = "시청 기록 저장 (오늘 본 룰)",
            description = """
                    경기 영상을 t초까지 봤다고 알립니다. AI가 그 구간에서 설명 카드로 보여 준 용어를
                    오늘 본 용어에 더하고, 오늘 본 용어 전체를 돌려줍니다.
                    학습 퀴즈는 오늘 본 용어로만 나옵니다 — 시청 종료·화면 이탈 때 호출하세요.
                    """
    )
    @PostMapping("/viewed-rules")
    public ResponseEntity<ViewedRulesResponse> recordViewed(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ViewedRulesRequest request
    ) {
        return ResponseEntity.ok(learningQuizService.recordViewed(userId, request));
    }

    @Operation(summary = "오늘 본 용어 조회")
    @GetMapping("/viewed-rules")
    public ResponseEntity<ViewedRulesResponse> getViewedRules(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(learningQuizService.getViewedRules(userId));
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

    @Operation(
            summary = "내 퀴즈 풀이 요약 조회",
            description = """
                    지정한 기간의 풀이 수, 정답 수, 오답 수, 정답률(%)을 반환합니다.
                    startDate와 endDate는 yyyy-MM-dd 형식이며 각각 생략할 수 있습니다.
                    저장된 submittedAt 기준으로 시작일과 종료일을 모두 포함합니다.
                    둘 다 생략하면 전체 기간의 통계를 반환합니다.
                    """
    )
    @GetMapping("/attempts/summary")
    public ResponseEntity<QuizAttemptSummaryResponse> getMyAttemptSummary(
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                learningQuizService.getMyAttemptSummary(userId, startDate, endDate)
        );
    }

    @Operation(
            summary = "내 퀴즈 풀이 기록 상세 조회",
            description = """
                본인의 풀이 기록과 문제 내용, 선택지, 정답 및 해설을 조회합니다.
                문제와 해설은 현재 등록된 내용을 기준으로 반환합니다.
                존재하지 않거나 다른 사용자의 기록이면 404를 반환합니다.
                """
    )
    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<QuizAttemptDetailResponse> getMyAttempt(
            @AuthenticationPrincipal Long userId,
            @PathVariable("attemptId") Long attemptId
    ) {
        return ResponseEntity.of(
                learningQuizService.getMyAttempt(userId, attemptId)
        );
    }
}