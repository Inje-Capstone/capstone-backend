package com.injecapstone.capstonebackend.quiz.domain;

import com.injecapstone.capstonebackend.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_quiz_attempts_user")
    )
    private User user;

    @Column(name = "question_id", nullable = false, updatable = false)
    private int questionId;

    @Column(name = "selected_option_id", nullable = false, updatable = false)
    private int selectedOptionId;

    @Column(nullable = false, updatable = false)
    private boolean correct;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    private QuizAttempt(
            User user,
            int questionId,
            int selectedOptionId,
            boolean correct
    ) {
        this.user = user;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.correct = correct;
    }

    public static QuizAttempt create(
            User user,
            int questionId,
            int selectedOptionId,
            boolean correct
    ) {
        return new QuizAttempt(user, questionId, selectedOptionId, correct);
    }

    @PrePersist
    protected void prePersist() {
        this.submittedAt = LocalDateTime.now();
    }
}