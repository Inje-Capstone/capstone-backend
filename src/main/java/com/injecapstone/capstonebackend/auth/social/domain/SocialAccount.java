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
        name = "social_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_provider_user",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialProvider provider;

    @Column(
            name = "provider_user_id",
            nullable = false,
            length = 255
    )
    private String providerUserId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private SocialAccount(
            User user,
            SocialProvider provider,
            String providerUserId
    ) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public static SocialAccount create(
            User user,
            SocialProvider provider,
            String providerUserId
    ) {
        return new SocialAccount(
                user,
                provider,
                providerUserId
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}