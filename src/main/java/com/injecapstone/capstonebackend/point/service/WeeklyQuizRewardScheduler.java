package com.injecapstone.capstonebackend.point.service;

import com.injecapstone.capstonebackend.quiz.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyQuizRewardScheduler {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final long MINIMUM_ATTEMPTS = 15;

    private final QuizAttemptRepository quizAttemptRepository;
    private final PointService pointService;

    @Scheduled(cron = "0 5 0,6,12,18 * * MON", zone = "Asia/Seoul")
    public void settlePreviousWeek() {
        settlePreviousWeek(Instant.now(), ZoneId.systemDefault());
    }

    void settlePreviousWeek(Instant now, ZoneId submissionZone) {
        LocalDate currentMonday = LocalDate.ofInstant(now, SEOUL)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate previousMonday = currentMonday.minusWeeks(1);

        LocalDateTime startAt = LocalDateTime.ofInstant(
                previousMonday.atStartOfDay(SEOUL).toInstant(), submissionZone
        );
        LocalDateTime endAt = LocalDateTime.ofInstant(
                currentMonday.atStartOfDay(SEOUL).toInstant(), submissionZone
        );

        for (var candidate : quizAttemptRepository.findWeeklyRewardCandidates(
                startAt, endAt, MINIMUM_ATTEMPTS
        )) {
            try {
                pointService.awardWeeklyQuizReward(
                        candidate.getUserId(), previousMonday, candidate.getAttemptCount(), now
                );
            } catch (RuntimeException exception) {
                log.error("주간 퀴즈 보상 정산 실패: userId={}, weekStart={}",
                        candidate.getUserId(), previousMonday, exception);
            }
        }
    }
}