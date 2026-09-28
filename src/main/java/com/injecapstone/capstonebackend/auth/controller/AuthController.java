package com.injecapstone.capstonebackend.auth.controller;

import com.injecapstone.capstonebackend.auth.dto.LoginRequest;
import com.injecapstone.capstonebackend.auth.dto.LoginResponse;
import com.injecapstone.capstonebackend.auth.dto.SignUpRequest;
import com.injecapstone.capstonebackend.auth.dto.SignUpResponse;
import com.injecapstone.capstonebackend.auth.service.AuthService;

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

    // 회원가입
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
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}