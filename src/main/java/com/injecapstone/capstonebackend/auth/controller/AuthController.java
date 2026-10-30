package com.injecapstone.capstonebackend.auth.controller;

import com.injecapstone.capstonebackend.auth.dto.LoginRequest;
import com.injecapstone.capstonebackend.auth.dto.LoginResponse;
import com.injecapstone.capstonebackend.auth.dto.SignUpRequest;
import com.injecapstone.capstonebackend.auth.dto.SignUpResponse;
import com.injecapstone.capstonebackend.auth.service.AuthService;
import com.injecapstone.capstonebackend.auth.social.dto.OAuthCodeExchangeRequest;
import com.injecapstone.capstonebackend.auth.social.dto.SocialLoginResponse;
import com.injecapstone.capstonebackend.auth.social.service.OAuthLoginCodeService;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuthLoginCodeService oauthLoginCodeService;

    // 회원가입
    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임으로 회원가입하고 생성된 회원 ID를 반환합니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        Long userId = authService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SignUpResponse(userId));
    }

    // 로그인
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 검증하고 회원 ID와 JWT 액세스 토큰을 반환합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 소셜 로그인 성공 후 전달된 일회용 로그인 코드를 교환
    @Operation(
            summary = "소셜 로그인 코드 교환",
            description = "소셜 로그인 성공 후 프론트엔드에 전달된 일회용 로그인 코드를 교환하여 로그인 결과를 반환합니다."
    )
    @PostMapping("/oauth/exchange")
    public ResponseEntity<SocialLoginResponse> exchangeOAuthCode(
            @Valid @RequestBody OAuthCodeExchangeRequest request
    ) {

        SocialLoginResponse response =
                oauthLoginCodeService.exchange(
                        request.code()
                );

        return ResponseEntity.ok(response);
    }
}