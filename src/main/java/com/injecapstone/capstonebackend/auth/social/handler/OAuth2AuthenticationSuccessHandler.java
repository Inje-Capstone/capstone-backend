package com.injecapstone.capstonebackend.auth.social.handler;

import com.injecapstone.capstonebackend.auth.social.domain.SocialProvider;
import com.injecapstone.capstonebackend.auth.social.dto.SocialLoginResult;
import com.injecapstone.capstonebackend.auth.social.service.OAuthLoginCodeService;
import com.injecapstone.capstonebackend.auth.social.service.SocialLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final SocialLoginService socialLoginService;
    private final OAuthLoginCodeService oauthLoginCodeService;

    /*
     * OAuth 로그인 완료 후 이동할 프론트엔드 주소
     *
     * 운영 환경:
     * https://injecapstone.com
     *
     * 로컬 환경에서는 application-local.properties에서
     * 별도의 로컬 프론트엔드 주소로 덮어쓸 수 있다.
     */
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        OAuth2User oauth2User =
                oauthToken.getPrincipal();

        String registrationId =
                oauthToken.getAuthorizedClientRegistrationId();

        SocialLoginResult result;

        /*
         * Google 로그인
         */
        if ("google".equals(registrationId)) {

            String providerUserId =
                    oauth2User.getAttribute("sub");

            String email =
                    oauth2User.getAttribute("email");

            String name =
                    oauth2User.getAttribute("name");

            result = socialLoginService.login(
                    SocialProvider.GOOGLE,
                    providerUserId,
                    email,
                    name
            );

            /*
             * Kakao 로그인
             */
        } else if ("kakao".equals(registrationId)) {

            Object id =
                    oauth2User.getAttribute("id");

            String providerUserId =
                    String.valueOf(id);

            Map<String, Object> kakaoAccount =
                    oauth2User.getAttribute("kakao_account");

            String email = null;
            String nickname = null;

            if (kakaoAccount != null) {

                Object emailValue =
                        kakaoAccount.get("email");

                if (emailValue != null) {
                    email = String.valueOf(emailValue);
                }

                Object profileObject =
                        kakaoAccount.get("profile");

                if (profileObject instanceof Map<?, ?> profile) {

                    Object nicknameValue =
                            profile.get("nickname");

                    if (nicknameValue != null) {
                        nickname =
                                String.valueOf(nicknameValue);
                    }
                }
            }

            result = socialLoginService.login(
                    SocialProvider.KAKAO,
                    providerUserId,
                    email,
                    nickname
            );

        } else {

            throw new IllegalArgumentException(
                    "지원하지 않는 소셜 로그인입니다."
            );
        }

        /*
         * OAuth 인증이 성공하면 JWT를 바로 URL에 전달하지 않는다.
         *
         * 대신 1회용 로그인 코드를 발급하고,
         * 프론트엔드가 해당 코드를 /api/auth/oauth/exchange API에 전달하여
         * 최종 JWT Access Token으로 교환하도록 한다.
         */
        String loginCode =
                oauthLoginCodeService.issue(
                        result.user(),
                        result.newUser()
                );

        /*
         * 프론트엔드 OAuth Callback 주소 생성
         *
         * 예:
         * https://injecapstone.com/oauth/callback?code=xxxxx
         */
        String redirectUrl =
                UriComponentsBuilder
                        .fromUriString(frontendUrl)
                        .path("/oauth/callback")
                        .queryParam("code", loginCode)
                        .build()
                        .encode()
                        .toUriString();

        /*
         * 기존 로컬 테스트용 JSON 응답 대신
         * 프론트엔드 Callback 페이지로 Redirect
         */
        response.sendRedirect(redirectUrl);
    }
}