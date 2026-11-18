package com.injecapstone.capstonebackend.ai.controller;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * /api/ai/** 중계 경로 검사.
 *
 * <p>프론트가 부르는 AI 경로만 허용한다(허용 목록). {@code /api/backend/**}는 정답이 든 백엔드 전용,
 * {@code /api/quiz}는 정답(answer_index)을 같이 주는 문항 생성이라, {@code /api/live/**}는 실시간 수집기
 * 전용(SSE·토큰)이라 중계하지 않는다. 학습 퀴즈는 백엔드 /api/quizzes가 채점한다.
 */
public final class AiProxyPaths {

    public static final String PREFIX = "/api/ai";

    // 중계 요청 본문 상한 (챗봇 질문 등 — 실제는 수백 바이트)
    public static final int MAX_BODY_BYTES = 64 * 1024;

    private static final List<String> ALLOWED = List.of(
            "/api/games",
            "/api/glossary",
            "/api/rules",
            "/api/cheer-songs"
    );

    private static final Pattern SAFE_PATH = Pattern.compile("^/api/[A-Za-z0-9_.~%/-]+$");

    private static final List<String> FORBIDDEN = List.of(
            "..", "//", "%2e", "%2f", "%5c", "%25"
    );

    private AiProxyPaths() {
    }

    /**
     * @param requestPath 컨텍스트 경로를 뺀 요청 경로(인코딩된 그대로), 예: {@code /api/ai/api/games}
     * @return AI 쪽 경로(예: {@code /api/games}), 허용하지 않으면 null
     */
    public static String target(String requestPath) {
        if (requestPath == null || !requestPath.startsWith(PREFIX + "/")) {
            return null;
        }

        String path = requestPath.substring(PREFIX.length());
        String lower = path.toLowerCase(Locale.ROOT);

        if (!SAFE_PATH.matcher(path).matches()
                || FORBIDDEN.stream().anyMatch(lower::contains)) {
            return null;
        }

        boolean allowed = ALLOWED.stream().anyMatch(prefix ->
                path.equals(prefix) || path.startsWith(prefix + "/")
        );

        return allowed ? path : null;
    }

    // 쿼리에 줄바꿈 등 제어 문자가 섞이면 거부
    public static boolean safeQuery(String query) {
        return query == null || query.chars().noneMatch(c -> c < 0x20 || c == 0x7f);
    }
}
