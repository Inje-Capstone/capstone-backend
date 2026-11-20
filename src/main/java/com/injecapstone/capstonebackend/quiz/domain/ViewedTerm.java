package com.injecapstone.capstonebackend.quiz.domain;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자가 그날 경기 시청 중 AI 설명 카드로 본 용어(룰) 하나.
 *
 * <p>학습 퀴즈는 그날 본 용어로만 낸다 — 본 게 없으면 퀴즈도 없다. 같은 날 같은 용어는 한 줄만 둔다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "viewed_terms",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_viewed_terms_user_date_term",
                columnNames = {"user_id", "viewed_date", "term_id"}
        )
)
public class ViewedTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_viewed_terms_user")
    )
    private User user;

    @Column(name = "term_id", nullable = false, updatable = false, length = 64)
    private String termId;

    @Column(name = "game_id", nullable = false, updatable = false, length = 64)
    private String gameId;

    @Column(name = "viewed_date", nullable = false, updatable = false)
    private LocalDate viewedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ViewedTerm(User user, String termId, String gameId, LocalDate viewedDate) {
        this.user = user;
        this.termId = termId;
        this.gameId = gameId;
        this.viewedDate = viewedDate;
    }

    public static ViewedTerm create(User user, String termId, String gameId, LocalDate viewedDate) {
        return new ViewedTerm(user, termId, gameId, viewedDate);
    }

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
