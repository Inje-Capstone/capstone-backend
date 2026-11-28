package com.injecapstone.capstonebackend.point.service;

import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.point.domain.PointTransaction;
import com.injecapstone.capstonebackend.point.dto.PointHistoryResponse;
import com.injecapstone.capstonebackend.point.repository.PointTransactionRepository;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String VIDEO_CLICK_PREFIX = "video-click:";
    private static final String VIDEO_INNING_PREFIX = "video-inning:";
    private static final String WEEKLY_QUIZ_PREFIX = "quiz-weekly:";
    private static final int DAILY_VIDEO_INNING_LIMIT = 9;
    private static final int MAX_HISTORY_PAGE_SIZE = 100;

    private final PointTransactionRepository pointTransactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public long getBalance(Long userId) {
        Long balance = pointTransactionRepository.sumAmountByUserId(userId);
        return balance == null ? 0L : balance;
    }

    @Transactional(readOnly = true)
    public PointHistoryResponse getHistory(Long userId, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_HISTORY_PAGE_SIZE
                || (long) page * size > Integer.MAX_VALUE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        var history = pointTransactionRepository.findByUser_IdOrderByRecordedAtUtcDescIdDesc(
                userId, PageRequest.of(page, size)
        );
        var items = history.getContent().stream()
                .map(entry -> new PointHistoryResponse.Item(
                        entry.getAmount(),
                        historyType(entry.getEventKey()),
                        entry.getRecordedAtUtc().toInstant(ZoneOffset.UTC)
                ))
                .toList();
        return new PointHistoryResponse(items, page, size, history.hasNext());
    }

    private static PointHistoryResponse.Type historyType(String eventKey) {
        if (eventKey.startsWith(VIDEO_CLICK_PREFIX)) {
            return PointHistoryResponse.Type.VIDEO_CLICK;
        }
        if (eventKey.startsWith(VIDEO_INNING_PREFIX)) {
            return PointHistoryResponse.Type.VIDEO_INNING;
        }
        if (eventKey.startsWith(WEEKLY_QUIZ_PREFIX)) {
            return PointHistoryResponse.Type.WEEKLY_QUIZ;
        }
        return PointHistoryResponse.Type.OTHER;
    }

    @Transactional
    public boolean awardVideoClick(Long userId, Instant clickedAt) {
        LocalDate rewardDate = LocalDate.ofInstant(clickedAt, SEOUL);
        String eventKey = VIDEO_CLICK_PREFIX + rewardDate;

        User user = userRepository.findByIdForPointUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (pointTransactionRepository.existsByUser_IdAndEventKey(userId, eventKey)) {
            return false;
        }

        pointTransactionRepository.save(new PointTransaction(user, 1, eventKey, clickedAt));
        return true;
    }

    @Transactional
    public boolean awardVideoInning(Long userId, String gameId, int inning, Instant awardedAt) {
        if (gameId == null || !gameId.matches("[A-Za-z0-9_-]{1,64}") || inning < 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Instant recordedAt = awardedAt.truncatedTo(ChronoUnit.MICROS);
        LocalDate rewardDate = LocalDate.ofInstant(recordedAt, SEOUL);
        LocalDateTime startUtc = LocalDateTime.ofInstant(
                rewardDate.atStartOfDay(SEOUL).toInstant(), ZoneOffset.UTC
        );
        LocalDateTime endUtc = LocalDateTime.ofInstant(
                rewardDate.plusDays(1).atStartOfDay(SEOUL).toInstant(), ZoneOffset.UTC
        );

        String encodedGameId = HexFormat.of().formatHex(gameId.getBytes(StandardCharsets.UTF_8));
        String eventKey = VIDEO_INNING_PREFIX + encodedGameId + ":" + inning;

        User user = userRepository.findByIdForPointUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (pointTransactionRepository.existsByUser_IdAndEventKey(userId, eventKey)) {
            return false;
        }

        long todayAwards = pointTransactionRepository.countAwardsBetween(
                userId, VIDEO_INNING_PREFIX, startUtc, endUtc
        );
        if (todayAwards >= DAILY_VIDEO_INNING_LIMIT) {
            return false;
        }

        pointTransactionRepository.save(new PointTransaction(user, 1, eventKey, recordedAt));
        return true;
    }

    @Transactional
    public int awardWeeklyQuizReward(
            Long userId, LocalDate weekStart, long submissionCount, Instant awardedAt
    ) {
        LocalDate today = LocalDate.ofInstant(awardedAt, SEOUL);
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY
                || weekStart.plusWeeks(1).isAfter(today)
                || submissionCount < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        int milestones = (int) Math.min(submissionCount / 15, 2);
        if (milestones == 0) {
            return 0;
        }

        User user = userRepository.findByIdForPointUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        int awardedPoints = 0;
        for (int milestone = 1; milestone <= milestones; milestone++) {
            String eventKey = WEEKLY_QUIZ_PREFIX + weekStart + ":" + milestone;
            if (!pointTransactionRepository.existsByUser_IdAndEventKey(userId, eventKey)) {
                pointTransactionRepository.save(new PointTransaction(user, 3, eventKey, awardedAt));
                awardedPoints += 3;
            }
        }
        return awardedPoints;
    }
}
