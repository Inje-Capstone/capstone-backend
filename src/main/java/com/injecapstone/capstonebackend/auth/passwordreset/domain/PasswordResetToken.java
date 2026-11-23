package com.injecapstone.capstonebackend.auth.passwordreset.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected PasswordResetToken() {
    }

    public PasswordResetToken(
            Long userId,
            String tokenHash,
            Instant issuedAt,
            Instant expiresAt
    ) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }
}