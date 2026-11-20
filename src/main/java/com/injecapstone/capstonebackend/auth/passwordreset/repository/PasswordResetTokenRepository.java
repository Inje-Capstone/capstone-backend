package com.injecapstone.capstonebackend.auth.passwordreset.repository;

import com.injecapstone.capstonebackend.auth.passwordreset.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    @Query("""
            select t.userId
            from PasswordResetToken t
            where t.tokenHash = :tokenHash
            """)
    Optional<Long> findUserIdByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("""
            delete from PasswordResetToken t
            where t.userId = :userId
              and t.tokenHash = :tokenHash
              and t.expiresAt > :now
            """)
    int consumeIfValid(
            @Param("userId") Long userId,
            @Param("tokenHash") String tokenHash,
            @Param("now") Instant now
    );
}