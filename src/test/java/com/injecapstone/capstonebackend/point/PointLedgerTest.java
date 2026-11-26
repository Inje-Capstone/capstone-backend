package com.injecapstone.capstonebackend.point;

import com.injecapstone.capstonebackend.point.domain.PointTransaction;
import com.injecapstone.capstonebackend.point.repository.PointTransactionRepository;
import com.injecapstone.capstonebackend.point.service.PointService;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDate;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PointService.class)
class PointLedgerTest {

    private static final Instant RECORDED_AT = Instant.parse("2026-11-23T01:00:00Z");

    @Test
    void awardsVideoClickOncePerAccountPerSeoulDay() {
        User first = createUser("video-first");
        User second = createUser("video-second");

        Instant beforeMidnight = Instant.parse("2026-11-24T14:59:58Z");
        Instant repeatClick = Instant.parse("2026-11-24T14:59:59Z");
        Instant nextDay = Instant.parse("2026-11-24T15:00:00Z");

        assertThat(points.awardVideoClick(first.getId(), beforeMidnight)).isTrue();
        assertThat(points.awardVideoClick(first.getId(), repeatClick)).isFalse();
        assertThat(points.awardVideoClick(second.getId(), repeatClick)).isTrue();
        assertThat(points.awardVideoClick(first.getId(), nextDay)).isTrue();

        assertThat(points.getBalance(first.getId())).isEqualTo(2L);
        assertThat(points.getBalance(second.getId())).isEqualTo(1L);
    }

    @Autowired
    private UserRepository users;

    @Autowired
    private PointTransactionRepository transactions;

    @Autowired
    private PointService points;

    @Test
    void sumsEachUsersOwnTransactionsAndReturnsZeroWithoutRecords() {
        User first = createUser("first");
        User second = createUser("second");
        User empty = createUser("empty");

        transactions.save(new PointTransaction(first, 3, "test:credit", RECORDED_AT));
        transactions.save(new PointTransaction(first, -1, "test:debit", RECORDED_AT));
        transactions.save(new PointTransaction(second, 7, "test:credit", RECORDED_AT));

        assertThat(points.getBalance(first.getId())).isEqualTo(2L);
        assertThat(points.getBalance(second.getId())).isEqualTo(7L);
        assertThat(points.getBalance(empty.getId())).isZero();
    }

    @Test
    void rejectsDuplicateEventForTheSameUser() {
        User user = createUser("duplicate");
        transactions.save(new PointTransaction(user, 1, "test:one-event", RECORDED_AT));

        assertThatThrownBy(() -> {
            transactions.save(new PointTransaction(user, 1, "test:one-event", RECORDED_AT));
            users.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void awardsWeeklyQuizMilestonesOnceAndCapsTheRewardAtSix() {
        User user = createUser("weekly-quiz");
        User other = createUser("weekly-other");
        LocalDate weekStart = LocalDate.of(2026, 11, 16);
        Instant awardedAt = Instant.parse("2026-11-23T00:00:00Z");

        assertThat(points.awardWeeklyQuizReward(user.getId(), weekStart, 14, awardedAt)).isZero();
        assertThat(points.awardWeeklyQuizReward(user.getId(), weekStart, 15, awardedAt)).isEqualTo(3);
        assertThat(points.awardWeeklyQuizReward(user.getId(), weekStart, 29, awardedAt)).isZero();
        assertThat(points.awardWeeklyQuizReward(user.getId(), weekStart, 30, awardedAt)).isEqualTo(3);
        assertThat(points.awardWeeklyQuizReward(user.getId(), weekStart, 45, awardedAt)).isZero();

        assertThat(points.awardWeeklyQuizReward(other.getId(), weekStart, 30, awardedAt)).isEqualTo(6);
        assertThat(points.getBalance(user.getId())).isEqualTo(6L);
        assertThat(points.getBalance(other.getId())).isEqualTo(6L);
    }

    private User createUser(String nickname) {
        return users.saveAndFlush(
                User.create(nickname + "@example.test", "test-password-hash", nickname)
        );
    }

    @Test
    void awardsEachGameInningOncePerAccountEvenAcrossDays() {
        User first = createUser("inning-first");
        User second = createUser("inning-second");
        Instant beforeMidnight = Instant.parse("2026-11-25T14:59:59Z");
        Instant afterMidnight = Instant.parse("2026-11-25T15:00:00Z");

        assertThat(points.awardVideoInning(first.getId(), "game-a", 1, beforeMidnight)).isTrue();
        assertThat(points.awardVideoInning(first.getId(), "game-a", 1, beforeMidnight)).isFalse();
        assertThat(points.awardVideoInning(first.getId(), "game-a", 1, afterMidnight)).isFalse();
        assertThat(points.awardVideoInning(first.getId(), "game-b", 1, beforeMidnight)).isTrue();
        assertThat(points.awardVideoInning(second.getId(), "game-a", 1, beforeMidnight)).isTrue();

        assertThat(points.getBalance(first.getId())).isEqualTo(2L);
        assertThat(points.getBalance(second.getId())).isEqualTo(1L);
    }

    @Test
    void capsInningAwardsAtNineAcrossGamesAndResetsAtSeoulMidnight() {
        User user = createUser("inning-limit");
        Instant beforeMidnight = Instant.parse("2026-11-25T14:59:59.999999Z");
        Instant afterMidnight = Instant.parse("2026-11-25T15:00:00Z");

        assertThat(points.awardVideoClick(user.getId(), beforeMidnight)).isTrue();

        for (int inning = 1; inning <= 5; inning++) {
            assertThat(points.awardVideoInning(user.getId(), "game-a", inning, beforeMidnight)).isTrue();
        }
        for (int inning = 1; inning <= 4; inning++) {
            assertThat(points.awardVideoInning(user.getId(), "game-b", inning, beforeMidnight)).isTrue();
        }

        assertThat(points.awardVideoInning(user.getId(), "game-b", 5, beforeMidnight)).isFalse();
        assertThat(points.getBalance(user.getId())).isEqualTo(10L);

        assertThat(points.awardVideoInning(user.getId(), "game-b", 5, afterMidnight)).isTrue();
        assertThat(points.getBalance(user.getId())).isEqualTo(11L);
    }
}