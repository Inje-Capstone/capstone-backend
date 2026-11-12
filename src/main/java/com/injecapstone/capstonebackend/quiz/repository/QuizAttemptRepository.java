package com.injecapstone.capstonebackend.quiz.repository;

import com.injecapstone.capstonebackend.quiz.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findTop20ByUser_IdOrderBySubmittedAtDescIdDesc(Long userId);
    Optional<QuizAttempt> findByIdAndUser_Id(Long attemptId, Long userId);
    @Query("""
        SELECT a.correct AS correct, COUNT(a) AS attemptCount
        FROM QuizAttempt a
        WHERE a.user.id = :userId
        GROUP BY a.correct
        """)
    List<AttemptCount> countByCorrectForUser(@Param("userId") Long userId);

    interface AttemptCount {
        boolean getCorrect();
        long getAttemptCount();
    }

    List<QuizAttempt> findTop20ByUser_IdAndCorrectOrderBySubmittedAtDescIdDesc(
            Long userId,
            boolean correct
    );
}