package com.injecapstone.capstonebackend.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * AI 엔진(capstone-AI) 연결 설정.
 *
 * @param baseUrl      AI 서버 주소. 비어 있으면 진단·학습 퀴즈는 임시 문항을 쓰고 /api/ai/** 중계는 503
 * @param timeout      진단·학습 퀴즈 호출 응답 대기 시간
 * @param proxyTimeout /api/ai/** 중계 응답 대기 시간 (챗봇·카드 생성은 LLM을 불러 오래 걸린다)
 */
@ConfigurationProperties(prefix = "ai")
public record AiProperties(
        @DefaultValue("") String baseUrl,
        @DefaultValue("5s") Duration timeout,
        @DefaultValue("30s") Duration proxyTimeout
) {

    public boolean enabled() {
        return baseUrl != null && !baseUrl.isBlank();
    }
}
