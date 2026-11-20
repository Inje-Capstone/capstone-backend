package com.injecapstone.capstonebackend.ai.controller;

import com.injecapstone.capstonebackend.ai.client.AiClient;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AiProxyController {

    private final AiClient aiClient;

    @Operation(
            summary = "AI 엔진 중계",
            description = """
                    /api/ai 뒤의 경로를 AI 엔진으로 그대로 넘깁니다.
                    예: GET /api/ai/api/games → AI GET /api/games
                    허용 경로: /api/games/**, /api/glossary/**, /api/rules/**, /api/cheer-songs
                    요청 본문은 64KB까지입니다. 로그인(Bearer JWT)이 필요합니다 — 없으면 401.
                    AI 서버에 연결할 수 없으면 503(AI_SERVICE_UNAVAILABLE)을 반환합니다.
                    """
    )
    @RequestMapping(
            value = AiProxyPaths.PREFIX + "/**",
            method = {RequestMethod.GET, RequestMethod.POST}
    )
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        String requestPath = request.getRequestURI()
                .substring(request.getContextPath().length());
        String path = AiProxyPaths.target(requestPath);

        if (path == null) {
            throw new BusinessException(ErrorCode.AI_ENDPOINT_NOT_FOUND);
        }

        String query = request.getQueryString();

        if (!AiProxyPaths.safeQuery(query)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        MediaType contentType = null;

        if (request.getContentType() != null) {
            try {
                contentType = MediaType.parseMediaType(request.getContentType());
            } catch (InvalidMediaTypeException exception) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
        }

        return aiClient.forward(
                HttpMethod.valueOf(request.getMethod()),
                query == null ? path : path + "?" + query,
                contentType,
                readBody(request)
        );
    }

    // 상한을 넘는 본문은 끝까지 읽지 않고 거부한다
    private static byte[] readBody(HttpServletRequest request) throws IOException {
        if (request.getContentLengthLong() > AiProxyPaths.MAX_BODY_BYTES) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        try (InputStream input = request.getInputStream()) {
            byte[] body = input.readNBytes(AiProxyPaths.MAX_BODY_BYTES + 1);

            if (body.length > AiProxyPaths.MAX_BODY_BYTES) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            return body;
        }
    }
}
