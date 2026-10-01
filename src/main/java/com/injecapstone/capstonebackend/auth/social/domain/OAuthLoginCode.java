package com.injecapstone.capstonebackend.auth.social.domain;

import com.injecapstone.capstonebackend.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "oauth_login_codes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_oauth_login_code_hash",
                        columnNames = "code_hash"
                )
        }
)
public class OAuthLoginCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "code_hash",
            nullable = false,
            length = 64
    )
    private String codeHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "is_new_user",
            nullable = false
    )
    private boolean newUser;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    private OAuthLoginCode(
            String codeHash,
            User user,
            boolean newUser,
            LocalDateTime expiresAt
    ) {
        this.codeHash = codeHash;
        this.user = user;
        this.newUser = newUser;
        this.expiresAt = expiresAt;
    }

    public static OAuthLoginCode create(
            String codeHash,
            User user,
            boolean newUser,
            LocalDateTime expiresAt
    ) {
        return new OAuthLoginCode(
                codeHash,
                user,
                newUser,
                expiresAt
        );
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void use() {
        this.usedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}