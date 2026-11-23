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

@RestController
@RequestMapping("/api/users/me/points")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PointController {

    private final PointService pointService;

    @Operation(
            summary = "내 보유 포인트 조회",
            description = "로그인한 사용자의 잔액을 조회합니다. 적립·차감 내역이 없으면 0을 반환합니다."
    )
    @GetMapping
    public PointBalanceResponse getMyPoints(@AuthenticationPrincipal Long userId) {
        return new PointBalanceResponse(pointService.getBalance(userId));
    }
}