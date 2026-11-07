package com.injecapstone.capstonebackend.quiz.repository;

import com.injecapstone.capstonebackend.quiz.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findTop20ByUser_IdOrderBySubmittedAtDescIdDesc(Long userId);
}