package com.injecapstone.capstonebackend.user.controller;

import com.injecapstone.capstonebackend.user.dto.MyProfileResponse;
import com.injecapstone.capstonebackend.user.dto.NicknameUpdateRequest;
import com.injecapstone.capstonebackend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // 로그인한 사용자의 기본 정보와 학습 설정 조회
    @Operation(
            summary = "내 정보 조회",
            description = "로그인한 사용자의 기본 정보, 학습 설정 및 온보딩 완료 여부를 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<MyProfileResponse> getMyProfile(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(
                userService.getMyProfile(userId)
        );
    }

    // 로그인한 사용자의 닉네임 변경
    @Operation(
            summary = "내 닉네임 수정",
            description = "닉네임을 2~20자로 변경하고 변경된 내 정보를 반환합니다."
    )
    @PatchMapping("/me/nickname")
    public ResponseEntity<MyProfileResponse> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NicknameUpdateRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateNickname(userId, request.nickname())
        );
    }
}