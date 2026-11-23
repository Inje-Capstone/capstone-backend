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

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PointService.class)
class PointLedgerTest {

    private static final Instant RECORDED_AT = Instant.parse("2026-11-23T01:00:00Z");

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

    private User createUser(String nickname) {
        return users.saveAndFlush(
                User.create(nickname + "@example.test", "test-password-hash", nickname)
        );
    }
}