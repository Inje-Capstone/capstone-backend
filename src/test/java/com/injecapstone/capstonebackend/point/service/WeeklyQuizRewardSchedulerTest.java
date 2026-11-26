package com.injecapstone.capstonebackend.point.service;

import com.injecapstone.capstonebackend.quiz.repository.QuizAttemptRepository;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PointService.class)
class WeeklyQuizRewardSchedulerTest {

    @Autowired private QuizAttemptRepository quizAttempts;
    @Autowired private UserRepository users;
    @Autowired private PointService points;
    @Autowired private EntityManager entityManager;

    @Test
    void settlesOnlyThePreviousSeoulWeekAndDoesNotPayItTwice() {
        User eligible = createUser("quiz-edge");
        User belowThreshold = createUser("quiz-below");

        LocalDateTime startUtc = LocalDateTime.parse("2026-11-15T15:00:00");
        LocalDateTime endUtc = LocalDateTime.parse("2026-11-22T15:00:00");
        Instant settlementTime = Instant.parse("2026-11-22T15:05:00Z");

        for (int questionId = 1; questionId <= 14; questionId++) {
            insertAttempt(eligible, questionId, startUtc.plusDays(1));
            insertAttempt(belowThreshold, questionId, startUtc.plusDays(1));
        }

        insertAttempt(eligible, 15, startUtc);
        insertAttempt(belowThreshold, 15, startUtc.minusSeconds(1));
        insertAttempt(belowThreshold, 16, endUtc);

        WeeklyQuizRewardScheduler scheduler = new WeeklyQuizRewardScheduler(quizAttempts, points);
        scheduler.settlePreviousWeek(settlementTime, ZoneOffset.UTC);

        assertThat(points.getBalance(eligible.getId())).isEqualTo(3L);
        assertThat(points.getBalance(belowThreshold.getId())).isZero();

        scheduler.settlePreviousWeek(settlementTime.plusSeconds(3600), ZoneOffset.UTC);

        assertThat(points.getBalance(eligible.getId())).isEqualTo(3L);
        assertThat(points.getBalance(belowThreshold.getId())).isZero();
    }

    private User createUser(String nickname) {
        return users.saveAndFlush(
                User.create(nickname + "@example.test", "test-password-hash", nickname)
        );
    }

    private void insertAttempt(User user, int questionId, LocalDateTime submittedAt) {
        entityManager.createNativeQuery("""
                INSERT INTO quiz_attempts
                    (user_id, question_id, selected_option_id, correct, submitted_at)
                VALUES (:userId, :questionId, 1, false, :submittedAt)
                """)
                .setParameter("userId", user.getId())
                .setParameter("questionId", questionId)
                .setParameter("submittedAt", submittedAt)
                .executeUpdate();
    }
}