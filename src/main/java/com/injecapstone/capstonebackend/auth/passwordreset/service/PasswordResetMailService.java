package com.injecapstone.capstonebackend.auth.passwordreset.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.password-reset.enabled", havingValue = "true")
public class PasswordResetMailService {

    private final PasswordResetTokenService tokenService;
    private final JavaMailSender mailSender;
    private final String frontendOrigin;
    private final String from;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 2, 0L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(32),
            Thread.ofPlatform().name("password-reset-mail-", 0).factory()
    );

    public PasswordResetMailService(
            PasswordResetTokenService tokenService,
            JavaMailSender mailSender,
            @Value("${app.password-reset.frontend-origin}") String frontendOrigin,
            @Value("${app.password-reset.from}") String from
    ) {
        this.tokenService = tokenService;
        this.mailSender = mailSender;
        this.frontendOrigin = frontendOrigin;
        this.from = from;
    }

    public void enqueue(String email) {
        executor.execute(() -> {
            try {
                tokenService.issue(email).ifPresent(this::sendResetLink);
            } catch (RuntimeException exception) {
                // 예외 메시지에 수신자 주소가 포함될 수 있으므로 종류만 기록한다.
                log.warn("비밀번호 재설정 메일 처리 실패: {}",
                        exception.getClass().getSimpleName());
            }
        });
    }

    private void sendResetLink(PasswordResetTokenService.IssuedToken issued) {
        String link = UriComponentsBuilder.fromUriString(frontendOrigin)
                .replacePath("/password-reset")
                .replaceQuery(null)
                .fragment(null)
                .queryParam("token", issued.token())
                .build()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(issued.email());
        message.setSubject("[LOOKIE] 비밀번호 재설정");
        message.setText("""
                아래 링크에서 새 비밀번호를 설정할 수 있습니다.
                링크는 발급 후 15분 동안 유효합니다.

                %s

                요청한 적이 없다면 이 메일을 무시해 주세요.
                """.formatted(link));
        mailSender.send(message);
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }
}