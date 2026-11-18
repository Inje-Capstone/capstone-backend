package com.injecapstone.capstonebackend.ai.client;

import com.injecapstone.capstonebackend.ai.dto.AiDiagnosisQuestion;
import com.injecapstone.capstonebackend.ai.dto.AiDiagnosisRequest;
import com.injecapstone.capstonebackend.ai.dto.AiDiagnosisResult;
import com.injecapstone.capstonebackend.ai.dto.AiQuizQuestion;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiClientTest {

    private static final String BASE = "http://ai";

    private MockRestServiceServer server;
    private AiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        client = new AiClient(BASE, restClient, restClient);
    }

    @Test
    void 진단_문항을_읽고_모르는_필드는_무시한다() {
        server.expect(requestTo(BASE + "/api/backend/onboarding/questions"))
                .andRespond(withSuccess("""
                        [{"questionId":1,"termId":"walk","question":"볼 4개면?","extra":true,
                          "options":[{"optionId":1,"text":"아웃"},{"optionId":2,"text":"1루"}]}]
                        """, MediaType.APPLICATION_JSON));

        List<AiDiagnosisQuestion> questions = client.diagnosisQuestions();

        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).question()).isEqualTo("볼 4개면?");
        assertThat(questions.get(0).options()).extracting("optionId").containsExactly(1, 2);
        server.verify();
    }

    @Test
    void 진단_채점은_답을_그대로_보내고_가중치는_무시한다() {
        server.expect(requestTo(BASE + "/api/backend/onboarding/diagnosis"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"answers":[{"questionId":1,"optionId":3}]}
                        """))
                .andRespond(withSuccess("""
                        {"correctCount":1,"totalCount":1,"learningLevel":"BEGINNER","levelLabel":"초보",
                         "message":"m","weights":{"threshold":0.5},
                         "results":[{"questionId":1,"termId":"walk","chosenOptionId":3,"correct":true}]}
                        """, MediaType.APPLICATION_JSON));

        AiDiagnosisResult result = client.gradeDiagnosis(List.of(new AiDiagnosisRequest.Answer(1, 3)));

        assertThat(result.learningLevel()).isEqualTo("BEGINNER");
        assertThat(result.results()).hasSize(1);
        assertThat(result.results().get(0).chosenOptionId()).isEqualTo(3);
    }

    @Test
    void 학습_퀴즈는_개수와_시드를_쿼리로_보낸다() {
        server.expect(requestTo(BASE + "/api/backend/quiz/questions?count=5&seed=7-2026-11-18"))
                .andRespond(withSuccess("""
                        [{"questionId":1001,"termId":"balk","text":"보크는?",
                          "options":[{"optionId":1,"text":"a"},{"optionId":2,"text":"b"}]}]
                        """, MediaType.APPLICATION_JSON));

        List<AiQuizQuestion> questions = client.quizQuestions(5, "7-2026-11-18");

        assertThat(questions).extracting("questionId").containsExactly(1001);
    }

    @Test
    void 없는_퀴즈_문항은_빈_값이고_서버_오류는_503으로_바꾼다() {
        server.expect(requestTo(BASE + "/api/backend/quiz/questions/1"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo(BASE + "/api/backend/quiz/questions/1001"))
                .andRespond(withServerError());

        assertThat(client.quizQuestion(1)).isEmpty();
        assertUnavailable(() -> client.quizQuestion(1001));
    }

    @Test
    void 퀴즈_문항_상세는_정답과_해설을_읽는다() {
        server.expect(requestTo(BASE + "/api/backend/quiz/questions/1001"))
                .andRespond(withSuccess("""
                        {"questionId":1001,"termId":"balk","text":"보크는?","correctOptionId":2,
                         "explanation":"해설","options":[{"optionId":1,"text":"a"},{"optionId":2,"text":"b"}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.quizQuestion(1001)).hasValueSatisfying(detail -> {
            assertThat(detail.correctOptionId()).isEqualTo(2);
            assertThat(detail.explanation()).isEqualTo("해설");
        });
    }

    @Test
    void 연결_실패와_빈_응답은_503() {
        server.expect(requestTo(BASE + "/api/backend/onboarding/questions"))
                .andRespond(withException(new IOException("connection refused")));
        server.expect(requestTo(BASE + "/api/backend/onboarding/questions"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertUnavailable(() -> client.diagnosisQuestions());
        assertUnavailable(() -> client.diagnosisQuestions());
    }

    @Test
    void 중계는_상태와_본문을_그대로_돌려준다() {
        server.expect(requestTo(BASE + "/api/games/x/state?t=10"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"detail\":\"없는 경기\"}"));
        server.expect(requestTo(BASE + "/api/games/x/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"question\":\"보크?\"}"))
                .andRespond(withSuccess("{\"answer\":\"a\"}", MediaType.APPLICATION_JSON));

        ResponseEntity<byte[]> missing = client.forward(
                HttpMethod.GET, "/api/games/x/state?t=10", null, null
        );
        ResponseEntity<byte[]> chat = client.forward(
                HttpMethod.POST, "/api/games/x/chat", MediaType.APPLICATION_JSON,
                "{\"question\":\"보크?\"}".getBytes(StandardCharsets.UTF_8)
        );

        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new String(missing.getBody(), StandardCharsets.UTF_8)).contains("없는 경기");
        assertThat(missing.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(chat.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    void 주소가_없으면_꺼진_상태이고_부르면_503() {
        AiClient disabled = AiClient.disabled();

        assertThat(disabled.enabled()).isFalse();
        assertUnavailable(disabled::diagnosisQuestions);
        assertUnavailable(() -> disabled.forward(HttpMethod.GET, "/api/games", null, null));
    }

    private static void assertUnavailable(Runnable call) {
        assertThatThrownBy(call::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }
}
