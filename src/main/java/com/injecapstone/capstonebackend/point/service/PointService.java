package com.injecapstone.capstonebackend.point.service;

import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.point.domain.PointTransaction;
import com.injecapstone.capstonebackend.point.repository.PointTransactionRepository;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String VIDEO_INNING_PREFIX = "video-inning:";
    private static final int DAILY_VIDEO_INNING_LIMIT = 9;

    private final PointTransactionRepository pointTransactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public long getBalance(Long userId) {
        Long balance = pointTransactionRepository.sumAmountByUserId(userId);
        return balance == null ? 0L : balance;
    }

    @Transactional
    public boolean awardVideoClick(Long userId, Instant clickedAt) {
        LocalDate rewardDate = LocalDate.ofInstant(clickedAt, SEOUL);
        String eventKey = "video-click:" + rewardDate;

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
}