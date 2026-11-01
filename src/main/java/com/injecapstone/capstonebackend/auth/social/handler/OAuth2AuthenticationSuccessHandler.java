package com.injecapstone.capstonebackend.auth.social.handler;

import com.injecapstone.capstonebackend.auth.social.domain.SocialProvider;
import com.injecapstone.capstonebackend.auth.social.dto.SocialLoginResult;
import com.injecapstone.capstonebackend.auth.social.service.OAuthLoginCodeService;
import com.injecapstone.capstonebackend.auth.social.service.SocialLoginService;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final SocialLoginService socialLoginService;
    private final OAuthLoginCodeService oauthLoginCodeService;
    private final OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        String redirectUrl;

        try {
            OAuth2AuthenticationToken oauthToken =
                    (OAuth2AuthenticationToken) authentication;

            SocialLoginResult result = loginSocialUser(oauthToken);

            // JWT 대신 일회용 로그인 코드를 전달한다.
            String loginCode = oauthLoginCodeService.issue(
                    result.user(),
                    result.newUser()
            );

            redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl)
                    .path("/oauth/callback")
                    .queryParam("code", loginCode)
                    .build()
                    .encode()
                    .toUriString();

        } catch (BusinessException exception) {

            String errorCode = exception.getErrorCode().name();

            log.warn(
                    "OAuth2 회원 처리 실패: errorCode={}",
                    errorCode
            );

            oauth2AuthenticationFailureHandler.redirectFailure(
                    request,
                    response,
                    errorCode
            );

            return;
        }

        response.sendRedirect(redirectUrl);
    }

    private SocialLoginResult loginSocialUser(
            OAuth2AuthenticationToken oauthToken
    ) {

        OAuth2User oauth2User = oauthToken.getPrincipal();

        String registrationId =
                oauthToken.getAuthorizedClientRegistrationId();

        if ("google".equals(registrationId)) {

            String providerUserId = oauth2User.getAttribute("sub");
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            return socialLoginService.login(
                    SocialProvider.GOOGLE,
                    providerUserId,
                    email,
                    name
            );
        }

        if ("kakao".equals(registrationId)) {

            Object id = oauth2User.getAttribute("id");
            String providerUserId = String.valueOf(id);

            Map<String, Object> kakaoAccount =
                    oauth2User.getAttribute("kakao_account");

            String email = null;
            String nickname = null;

            if (kakaoAccount != null) {

                Object emailValue = kakaoAccount.get("email");

                if (emailValue != null) {
                    email = String.valueOf(emailValue);
                }

                Object profileObject = kakaoAccount.get("profile");

                if (profileObject instanceof Map<?, ?> profile) {

                    Object nicknameValue = profile.get("nickname");

                    if (nicknameValue != null) {
                        nickname = String.valueOf(nicknameValue);
                    }
                }
            }

            return socialLoginService.login(
                    SocialProvider.KAKAO,
                    providerUserId,
                    email,
                    nickname
            );
        }

        throw new IllegalArgumentException(
                "지원하지 않는 소셜 로그인입니다."
        );
    }
}