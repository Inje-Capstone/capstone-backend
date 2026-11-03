package com.injecapstone.capstonebackend.quiz.controller;

import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse;
import com.injecapstone.capstonebackend.quiz.service.LearningQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}