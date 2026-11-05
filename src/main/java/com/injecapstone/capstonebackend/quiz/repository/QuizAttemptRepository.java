package com.injecapstone.capstonebackend.quiz.repository;

import com.injecapstone.capstonebackend.quiz.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
}