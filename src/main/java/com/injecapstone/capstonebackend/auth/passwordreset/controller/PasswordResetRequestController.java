package com.injecapstone.capstonebackend.auth.passwordreset.controller;

import com.injecapstone.capstonebackend.auth.passwordreset.service.PasswordResetMailService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.injecapstone.capstonebackend.auth.passwordreset.service.PasswordResetTokenService;
import jakarta.validation.constraints.Pattern;

import java.util.concurrent.RejectedExecutionException;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.password-reset.enabled", havingValue = "true")
public class PasswordResetRequestController {

    private final PasswordResetMailService mailService;
    private final PasswordResetTokenService tokenService;

    @Operation(summary = "비밀번호 재설정 링크 요청")
    @PostMapping("/request")
    public ResponseEntity<Void> request(@Valid @RequestBody ResetLinkRequest request) {
        try {
            mailService.enqueue(request.email());
            return ResponseEntity.accepted().build();
        } catch (RejectedExecutionException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    public record ResetLinkRequest(
            @NotBlank @Email @Size(max = 254) String email
    ) {

    }
    @Operation(summary = "재설정 토큰으로 새 비밀번호 저장")
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        tokenService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    public record ResetPasswordRequest(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{43}") String token,
            @NotBlank @Size(min = 8, max = 20) String newPassword
    ) {
        @Override
        public String toString() {
            return "ResetPasswordRequest[redacted]";
        }
    }
}