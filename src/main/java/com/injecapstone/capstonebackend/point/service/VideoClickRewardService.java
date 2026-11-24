package com.injecapstone.capstonebackend.point.service;

import com.injecapstone.capstonebackend.ai.client.AiClient;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.point.dto.VideoClickResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VideoClickRewardService {

    private final AiClient aiClient;
    private final PointService pointService;

    public VideoClickResponse reward(Long userId, String gameId) {
        Instant clickedAt = Instant.now();

        if (!aiClient.hasGameVideo(gameId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        boolean awarded = pointService.awardVideoClick(userId, clickedAt);
        return new VideoClickResponse(awarded, pointService.getBalance(userId));
    }
}