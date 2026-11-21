package com.injecapstone.capstonebackend.auth.passwordreset.service;

import com.injecapstone.capstonebackend.auth.passwordreset.domain.PasswordResetToken;
import com.injecapstone.capstonebackend.auth.passwordreset.repository.PasswordResetTokenRepository;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    static final Duration TOKEN_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_INTERVAL = Duration.ofMinutes(1);
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;

    @Transactional
    public Optional<IssuedToken> issue(String email) {
        User user = userRepository.findByEmailForPasswordReset(email).orElse(null);
        if (user == null) {
            return Optional.empty();
        }

        Instant now = Instant.now();
        boolean requestedRecently = tokenRepository.findById(user.getId())
                .map(saved -> saved.getIssuedAt().plus(RESEND_INTERVAL).isAfter(now))
                .orElse(false);

        if (requestedRecently) {
            return Optional.empty();
        }

        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        tokenRepository.save(new PasswordResetToken(
                user.getId(),
                hashToken(token),
                now,
                now.plus(TOKEN_TTL)
        ));

        return Optional.of(new IssuedToken(user.getEmail(), token));
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String tokenHash = hashToken(token);

        Long userId = tokenRepository.findUserIdByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        User user = userRepository.findByIdForPasswordReset(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        int consumed = tokenRepository.consumeIfValid(userId, tokenHash, Instant.now());
        if (consumed != 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        user.changePassword(passwordEncoder.encode(newPassword));
    }

    static String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", exception);
        }
    }

    public record IssuedToken(String email, String token) {
        @Override
        public String toString() {
            return "IssuedToken[redacted]";
        }
    }
}