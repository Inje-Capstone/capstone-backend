package com.injecapstone.capstonebackend.quiz.repository;

import com.injecapstone.capstonebackend.quiz.domain.QuizAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    @Query("""
            SELECT a
            FROM QuizAttempt a
            WHERE a.user.id = :userId
              AND (:correct IS NULL OR a.correct = :correct)
              AND (:startAt IS NULL OR a.submittedAt >= :startAt)
              AND (:endAt IS NULL OR a.submittedAt <= :endAt)
            ORDER BY a.submittedAt DESC, a.id DESC
            """)
    List<QuizAttempt> findMyAttempts(
            @Param("userId") Long userId,
            @Param("correct") Boolean correct,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            Pageable pageable
    );

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
}