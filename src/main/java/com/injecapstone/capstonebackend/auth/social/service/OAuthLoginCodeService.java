package com.injecapstone.capstonebackend.auth.social.service;

import com.injecapstone.capstonebackend.auth.social.domain.OAuthLoginCode;
import com.injecapstone.capstonebackend.auth.social.repository.OAuthLoginCodeRepository;
import com.injecapstone.capstonebackend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.injecapstone.capstonebackend.auth.social.dto.SocialLoginResponse;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.global.jwt.JwtTokenProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthLoginCodeService {

    private static final int CODE_BYTE_LENGTH = 32;
    private static final long EXPIRATION_MINUTES = 5;

    private final OAuthLoginCodeRepository oauthLoginCodeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String issue(
            User user,
            boolean newUser
    ) {

        String rawCode = generateRandomCode();

        String codeHash = hash(rawCode);

        OAuthLoginCode loginCode =
                OAuthLoginCode.create(
                        codeHash,
                        user,
                        newUser,
                        LocalDateTime.now()
                                .plusMinutes(EXPIRATION_MINUTES)
                );

        oauthLoginCodeRepository.save(loginCode);

        return rawCode;
    }

    @Transactional
    public SocialLoginResponse exchange(String rawCode) {

        String codeHash = hash(rawCode);

        OAuthLoginCode loginCode =
                oauthLoginCodeRepository.findByCodeHash(codeHash)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVALID_OAUTH_LOGIN_CODE
                                )
                        );

        if (loginCode.isUsed() ||
                loginCode.isExpired()) {

            throw new BusinessException(
                    ErrorCode.INVALID_OAUTH_LOGIN_CODE
            );
        }

        loginCode.use();

        User user = loginCode.getUser();

        String accessToken =
                jwtTokenProvider.createAccessToken(user);

        return new SocialLoginResponse(
                user.getId(),
                accessToken,
                loginCode.isNewUser()
        );
    }

    public String hash(String rawCode) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashedBytes =
                    digest.digest(
                            rawCode.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hashedBytes);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    e
            );
        }
    }

    private String generateRandomCode() {

        byte[] bytes =
                new byte[CODE_BYTE_LENGTH];

        secureRandom.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}