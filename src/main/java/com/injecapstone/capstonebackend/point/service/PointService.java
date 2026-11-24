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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

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
}