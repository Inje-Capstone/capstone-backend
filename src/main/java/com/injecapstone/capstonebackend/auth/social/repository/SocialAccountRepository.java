package com.injecapstone.capstonebackend.auth.social.repository;

import com.injecapstone.capstonebackend.auth.social.domain.SocialAccount;
import com.injecapstone.capstonebackend.auth.social.domain.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository
        extends JpaRepository<SocialAccount, Long> {

    // 소셜 플랫폼과 사용자 고유 ID로 계정 조회
    Optional<SocialAccount> findByProviderAndProviderUserId(
            SocialProvider provider,
            String providerUserId
    );

    // 사용자가 특정 소셜 플랫폼의 계정을 연결했는지 확인
    boolean existsByUser_IdAndProvider(
            Long userId,
            SocialProvider provider
    );
}