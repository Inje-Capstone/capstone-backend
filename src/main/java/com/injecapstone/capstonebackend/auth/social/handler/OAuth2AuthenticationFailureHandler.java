package com.injecapstone.capstonebackend.auth.social.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        String errorCode =
                exception instanceof OAuth2AuthenticationException oauthException
                        ? oauthException.getError().getErrorCode()
                        : exception.getClass().getSimpleName();

        // 토큰이나 인증 코드 대신 예외 유형과 오류 코드만 기록한다.
        log.warn(
                "OAuth2 인증 실패: type={}, errorCode={}",
                exception.getClass().getSimpleName(),
                errorCode
        );

        redirectFailure(
                request,
                response,
                "OAUTH_AUTHENTICATION_FAILED"
        );
    }

    public void redirectFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            String errorCode
    ) throws IOException {

        // 실패한 OAuth 인증 정보와 임시 세션을 정리한다.
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/oauth/callback")
                .queryParam("error", errorCode)
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}