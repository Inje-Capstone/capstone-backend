package com.injecapstone.capstonebackend.auth.social.service;

import com.injecapstone.capstonebackend.auth.social.domain.SocialAccount;
import com.injecapstone.capstonebackend.auth.social.domain.SocialProvider;
import com.injecapstone.capstonebackend.auth.social.dto.SocialLoginResult;
import com.injecapstone.capstonebackend.auth.social.repository.SocialAccountRepository;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialLoginService {

    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;

    @Transactional
    public SocialLoginResult login(
            SocialProvider provider,
            String providerUserId,
            String email,
            String name
    ) {

        return socialAccountRepository
                .findByProviderAndProviderUserId(
                        provider,
                        providerUserId
                )
                .map(socialAccount ->
                        new SocialLoginResult(
                                socialAccount.getUser(),
                                false
                        )
                )
                .orElseGet(() ->
                        createSocialUser(
                                provider,
                                providerUserId,
                                email,
                                name
                        )
                );
    }

    private SocialLoginResult createSocialUser(
            SocialProvider provider,
            String providerUserId,
            String email,
            String name
    ) {

        if (email != null &&
                userRepository.existsByEmail(email)) {

            throw new BusinessException(
                    ErrorCode.SOCIAL_ACCOUNT_LINK_REQUIRED
            );
        }

        String nickname =
                createUniqueNickname(
                        name,
                        providerUserId
                );

        User user = User.createSocial(
                email,
                nickname
        );

        User savedUser =
                userRepository.save(user);

        SocialAccount socialAccount =
                SocialAccount.create(
                        savedUser,
                        provider,
                        providerUserId
                );

        socialAccountRepository.save(socialAccount);

        return new SocialLoginResult(
                savedUser,
                true
        );
    }

    private String createUniqueNickname(
            String name,
            String providerUserId
    ) {

        String nickname =
                (name == null || name.isBlank())
                        ? "루키"
                        : name.trim();

        if (nickname.length() > 20) {
            nickname = nickname.substring(0, 20);
        }

        if (!userRepository.existsByNickname(nickname)) {
            return nickname;
        }

        String suffix =
                providerUserId.substring(
                        Math.max(
                                0,
                                providerUserId.length() - 8
                        )
                );

        return "rookie_" + suffix;
    }
}