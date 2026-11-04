package com.injecapstone.capstonebackend.quiz.service;

import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse.Option;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningQuizService {

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

    public List<QuizQuestionResponse> getQuestions() {
        return QUESTIONS;
    }
}