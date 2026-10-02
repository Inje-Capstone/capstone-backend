package com.injecapstone.capstonebackend.auth.social.handler;

import com.injecapstone.capstonebackend.auth.social.domain.SocialProvider;
import com.injecapstone.capstonebackend.auth.social.dto.SocialLoginResult;
import com.injecapstone.capstonebackend.auth.social.service.OAuthLoginCodeService;
import com.injecapstone.capstonebackend.auth.social.service.SocialLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final SocialLoginService socialLoginService;
    private final OAuthLoginCodeService oauthLoginCodeService;

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

        String loginCode =
                oauthLoginCodeService.issue(
                        result.user(),
                        result.newUser()
                );

        // 로컬 테스트용
        // 다음 단계에서 프론트엔드 Redirect 방식으로 변경 예정
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(
                "application/json;charset=UTF-8"
        );

        response.getWriter().write(
                """
                {
                  "code": "%s"
                }
                """.formatted(loginCode)
        );
    }
}