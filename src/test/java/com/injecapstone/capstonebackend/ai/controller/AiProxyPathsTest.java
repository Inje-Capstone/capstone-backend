package com.injecapstone.capstonebackend.ai.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiProxyPathsTest {

    @Test
    void 프론트가_부르는_경로는_앞의_api_ai만_떼고_넘긴다() {
        assertThat(AiProxyPaths.target("/api/ai/api/games")).isEqualTo("/api/games");
        assertThat(AiProxyPaths.target("/api/ai/api/games/20260823LGOB/cards"))
                .isEqualTo("/api/games/20260823LGOB/cards");
        assertThat(AiProxyPaths.target("/api/ai/api/glossary/balk")).isEqualTo("/api/glossary/balk");
        assertThat(AiProxyPaths.target("/api/ai/api/cheer-songs")).isEqualTo("/api/cheer-songs");
        assertThat(AiProxyPaths.target("/api/ai/api/glossary/%EB%B3%B4%ED%81%AC"))
                .isEqualTo("/api/glossary/%EB%B3%B4%ED%81%AC");
    }

    @Test
    void 정답이_든_백엔드_전용_경로와_실시간_경로는_막는다() {
        assertThat(AiProxyPaths.target("/api/ai/api/backend/quiz/questions/1001")).isNull();
        assertThat(AiProxyPaths.target("/api/ai/api/backend/onboarding/questions")).isNull();
        assertThat(AiProxyPaths.target("/api/ai/api/live/LIVE1/stream")).isNull();
        assertThat(AiProxyPaths.target("/api/ai/api/quiz")).isNull();
        assertThat(AiProxyPaths.target("/api/ai/health")).isNull();
        assertThat(AiProxyPaths.target("/api/ai/api/gamesx")).isNull();
    }

    @Test
    void 경로_조작은_거부한다() {
        assertThat(AiProxyPaths.target("/api/ai/api/games/../backend/quiz")).isNull();
        assertThat(AiProxyPaths.target("/api/ai/api/games/%2e%2e/backend")).isNull();
        assertThat(AiProxyPaths.target("/api/ai/api/games/%2F..")).isNull();
        assertThat(AiProxyPaths.target("/api/ai/api/games/%252e%252e")).isNull();
        assertThat(AiProxyPaths.target("/api/ai/api/games//x")).isNull();
        assertThat(AiProxyPaths.target("/api/ai/api/games;jsessionid=1")).isNull();
        assertThat(AiProxyPaths.target("/api/ai")).isNull();
        assertThat(AiProxyPaths.target("/api/aix/api/games")).isNull();
        assertThat(AiProxyPaths.target(null)).isNull();
    }

    @Test
    void 쿼리의_제어_문자는_거부한다() {
        assertThat(AiProxyPaths.safeQuery(null)).isTrue();
        assertThat(AiProxyPaths.safeQuery("t=10&level=INTRODUCTORY&category=%EA%B8%B0")).isTrue();
        assertThat(AiProxyPaths.safeQuery("t=1\r\nX-Evil: 1")).isFalse();
    }
}
