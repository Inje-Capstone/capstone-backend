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
import com.injecapstone.capstonebackend.user.dto.GameNotificationUpdateRequest;
import com.injecapstone.capstonebackend.user.dto.SupportedTeamRequest;
import com.injecapstone.capstonebackend.user.dto.SupportedTeamResponse;
import com.injecapstone.capstonebackend.user.dto.LearningLevelResponse;

import java.util.List;

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
    // 로그인한 사용자의 경기 알림 수신 설정 변경
    @Operation(
            summary = "경기 알림 수신 설정 변경",
            description = "경기 알림 수신 여부를 변경하고 변경된 내 정보를 반환합니다."
    )
    @PatchMapping("/me/game-notification")
    public ResponseEntity<MyProfileResponse> updateGameNotification(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody GameNotificationUpdateRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateGameNotification(
                        userId,
                        request.gameNotificationEnabled()
                )
        );
    }

    // 로그인한 사용자의 응원팀 변경
    @Operation(
            summary = "내 응원팀 수정",
            description = "응원팀을 변경하고 변경된 내 정보를 반환합니다. NONE은 응원팀이 없음을 의미합니다."
    )
    @PatchMapping("/me/supported-team")
    public ResponseEntity<MyProfileResponse> updateSupportedTeam(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SupportedTeamRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateSupportedTeam(
                        userId,
                        request.supportedTeam()
                )
        );
    }

    // 온보딩 및 마이페이지에서 사용할 응원팀 선택 목록 조회
    @Operation(
            summary = "응원팀 선택 목록 조회",
            description = "응원팀 코드와 표시 이름을 반환합니다. NONE(아직 없어요)을 포함합니다."
    )
    @GetMapping("/supported-teams")
    public ResponseEntity<List<SupportedTeamResponse>> getSupportedTeams() {
        return ResponseEntity.ok(
                userService.getSupportedTeams()
        );
    }

    // 온보딩 및 마이페이지에서 사용할 학습 수준 선택 목록 조회
    @Operation(
            summary = "학습 수준 선택 목록 조회",
            description = "학습 수준 코드와 표시 이름을 입문, 초보, 익숙 순서로 반환합니다."
    )
    @GetMapping("/learning-levels")
    public ResponseEntity<List<LearningLevelResponse>> getLearningLevels() {
        return ResponseEntity.ok(
                userService.getLearningLevels()
        );
    }
}