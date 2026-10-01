package com.injecapstone.capstonebackend.auth.social.repository;

import com.injecapstone.capstonebackend.auth.social.domain.OAuthLoginCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface OAuthLoginCodeRepository
        extends JpaRepository<OAuthLoginCode, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OAuthLoginCode> findByCodeHash(
            String codeHash
    );
}