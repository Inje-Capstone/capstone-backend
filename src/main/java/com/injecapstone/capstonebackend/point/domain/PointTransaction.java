package com.injecapstone.capstonebackend.point.domain;

import com.injecapstone.capstonebackend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "point_transactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_point_transactions_user_event",
                columnNames = {"user_id", "event_key"}
        )
)
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_point_transactions_user")
    )
    private User user;

    @Column(nullable = false, updatable = false)
    private int amount;

    @Column(name = "event_key", nullable = false, updatable = false, length = 191)
    private String eventKey;

    @Column(name = "recorded_at_utc", nullable = false, updatable = false)
    private LocalDateTime recordedAtUtc;

    public PointTransaction(User user, int amount, String eventKey, Instant recordedAt) {
        if (amount == 0) {
            throw new IllegalArgumentException("Point transaction amount must be nonzero");
        }
        if (eventKey == null || !eventKey.matches("[a-z0-9][a-z0-9:_-]{0,190}")) {
            throw new IllegalArgumentException("Invalid point event key");
        }

        this.user = Objects.requireNonNull(user);
        this.amount = amount;
        this.eventKey = eventKey;
        this.recordedAtUtc = LocalDateTime.ofInstant(
                Objects.requireNonNull(recordedAt),
                ZoneOffset.UTC
        );
    }
}