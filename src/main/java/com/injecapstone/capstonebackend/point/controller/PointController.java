package com.injecapstone.capstonebackend.point.controller;

import com.injecapstone.capstonebackend.point.dto.PointBalanceResponse;
import com.injecapstone.capstonebackend.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.injecapstone.capstonebackend.point.dto.VideoClickRequest;
import com.injecapstone.capstonebackend.point.dto.VideoClickResponse;
import com.injecapstone.capstonebackend.point.service.VideoClickRewardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/users/me/points")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PointController {

    private final PointService pointService;
    private final VideoClickRewardService videoClickRewardService;



    @Operation(
            summary = "내 보유 포인트 조회",
            description = "로그인한 사용자의 잔액을 조회합니다. 적립·차감 내역이 없으면 0을 반환합니다."
    )
    @GetMapping
    public PointBalanceResponse getMyPoints(@AuthenticationPrincipal Long userId) {
        return new PointBalanceResponse(pointService.getBalance(userId));
    }

    @Operation(
            summary = "영상 클릭 포인트 적립",
            description = "영상이 있는 경기의 클릭에 대해 한국 시간 기준 계정당 하루 한 번 1포인트를 지급합니다."
    )
    @PostMapping("/video-click")
    public VideoClickResponse rewardVideoClick(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody VideoClickRequest request
    ) {
        return videoClickRewardService.reward(userId, request.gameId());
    }
}