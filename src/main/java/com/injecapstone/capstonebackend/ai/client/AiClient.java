package com.injecapstone.capstonebackend.ai.client;

import com.injecapstone.capstonebackend.ai.dto.AiDiagnosisQuestion;
import com.injecapstone.capstonebackend.ai.dto.AiDiagnosisRequest;
import com.injecapstone.capstonebackend.ai.dto.AiDiagnosisResult;
import com.injecapstone.capstonebackend.ai.dto.AiQuizQuestion;
import com.injecapstone.capstonebackend.ai.dto.AiQuizQuestionDetail;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.injecapstone.capstonebackend.ai.dto.AiGameSummary;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * AI 엔진(capstone-AI) HTTP 클라이언트.
 *
 * <p>AI 서버는 상태를 갖지 않는다. 문항·채점·설명 생성만 맡고 저장(풀이 기록·학습 수준)은 백엔드가 한다.
 * 연결 실패·시간 초과·잘못된 응답은 모두 {@link ErrorCode#AI_SERVICE_UNAVAILABLE}(503)로 바꾼다.
 */
@Slf4j
public class AiClient {

    private static final String DIAGNOSIS_QUESTIONS = "/api/backend/onboarding/questions";
    private static final String DIAGNOSIS = "/api/backend/onboarding/diagnosis";
    private static final String QUIZ_QUESTIONS = "/api/backend/quiz/questions";

    private final String baseUrl;
    private final RestClient api;
    private final RestClient proxy;

    public AiClient(String baseUrl, RestClient api, RestClient proxy) {
        this.baseUrl = baseUrl;
        this.api = api;
        this.proxy = proxy;
    }

    // AI 주소가 설정되지 않은 환경(로컬 개발 등)
    public static AiClient disabled() {
        return new AiClient(null, null, null);
    }

    public boolean enabled() {
        return api != null;
    }

    public List<AiDiagnosisQuestion> diagnosisQuestions() {
        List<AiDiagnosisQuestion> questions = call(() -> api.get()
                .uri(DIAGNOSIS_QUESTIONS)
                .retrieve()
                .body(new ParameterizedTypeReference<List<AiDiagnosisQuestion>>() {
                })
        );
        return requireNonEmpty(questions);
    }

    public AiDiagnosisResult gradeDiagnosis(List<AiDiagnosisRequest.Answer> answers) {
        AiDiagnosisResult result = call(() -> api.post()
                .uri(DIAGNOSIS)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AiDiagnosisRequest(answers))
                .retrieve()
                .body(AiDiagnosisResult.class)
        );
        if (result == null || result.results() == null || result.learningLevel() == null) {
            throw unavailable("진단 채점 응답이 비어 있음");
        }
        return result;
    }

    public List<AiQuizQuestion> quizQuestions(int count, String seed) {
        List<AiQuizQuestion> questions = call(() -> api.get()
                .uri(builder -> builder.path(QUIZ_QUESTIONS)
                        .queryParam("count", count)
                        .queryParam("seed", seed)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<AiQuizQuestion>>() {
                })
        );
        return requireNonEmpty(questions);
    }

    public boolean hasGameVideo(String gameId) {
        List<AiGameSummary> games = call(() -> api.get()
                .uri("/api/games")
                .retrieve()
                .body(new ParameterizedTypeReference<List<AiGameSummary>>() {
                })
        );

        if (games == null) {
            throw unavailable("경기 목록 응답이 비어 있음");
        }

        return games.stream().anyMatch(game ->
                game != null && gameId.equals(game.id()) && game.hasVideo()
        );
    }

    // 없는 문항이면 빈 값 — 호출하는 쪽이 임시 문항(예전 풀이 기록)으로 넘어갈 수 있게
    public Optional<AiQuizQuestionDetail> quizQuestion(int questionId) {
        return call(() -> api.get()
                .uri(QUIZ_QUESTIONS + "/{questionId}", questionId)
                .exchange((request, response) -> {
                    if (response.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                        return Optional.<AiQuizQuestionDetail>empty();
                    }
                    if (response.getStatusCode().isError()) {
                        throw unavailable("퀴즈 문항 조회 HTTP " + response.getStatusCode().value());
                    }
                    return Optional.ofNullable(response.bodyTo(AiQuizQuestionDetail.class));
                })
        );
    }

    /**
     * 프론트 요청을 AI로 그대로 넘긴다. 경로 검사는 호출하는 쪽({@code AiProxyPaths})이 끝낸 뒤다.
     *
     * @param pathAndQuery {@code /api/...}로 시작하는 인코딩된 경로(+ 쿼리)
     */
    public ResponseEntity<byte[]> forward(
            HttpMethod method,
            String pathAndQuery,
            MediaType contentType,
            byte[] body
    ) {
        if (!enabled()) {
            throw unavailable("AI 주소가 설정되지 않음");
        }

        URI uri;
        try {
            uri = URI.create(baseUrl + pathAndQuery);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return call(() -> {
            RestClient.RequestBodySpec spec = proxy.method(method)
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON);

            if (body != null && body.length > 0) {
                spec.contentType(contentType == null ? MediaType.APPLICATION_JSON : contentType)
                        .body(body);
            }

            return spec.exchange((request, response) -> {
                HttpHeaders headers = new HttpHeaders();
                MediaType type = response.getHeaders().getContentType();
                if (type != null) {
                    headers.setContentType(type);
                }
                return ResponseEntity.status(response.getStatusCode())
                        .headers(headers)
                        .body(response.getBody().readAllBytes());
            });
        });
    }

    private <T> T call(Supplier<T> request) {
        if (!enabled()) {
            throw unavailable("AI 주소가 설정되지 않음");
        }
        try {
            return request.get();
        } catch (RestClientException exception) {
            throw unavailable(exception.getMessage());
        }
    }

    private <T> List<T> requireNonEmpty(List<T> items) {
        if (items == null || items.isEmpty()) {
            throw unavailable("문항 응답이 비어 있음");
        }
        return items;
    }

    private BusinessException unavailable(String reason) {
        log.warn("AI 엔진 호출 실패: {}", reason);
        return new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }
}
