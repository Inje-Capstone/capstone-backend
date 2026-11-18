package com.injecapstone.capstonebackend.ai.controller;

import com.injecapstone.capstonebackend.ai.client.AiClient;
import com.injecapstone.capstonebackend.global.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiProxyControllerTest {

    private final AiClient ai = mock(AiClient.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AiProxyController(ai))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void 경로와_쿼리를_그대로_넘기고_응답을_돌려준다() throws Exception {
        when(ai.forward(eq(HttpMethod.GET), eq("/api/games/g1/cards?t=10&level=INTRODUCTORY"),
                any(), any()))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"cards\":[]}".getBytes(StandardCharsets.UTF_8)));

        mvc.perform(get("/api/ai/api/games/g1/cards?t=10&level=INTRODUCTORY"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"cards\":[]}"));
    }

    @Test
    void 챗봇_본문을_넘긴다() throws Exception {
        byte[] body = "{\"question\":\"보크?\"}".getBytes(StandardCharsets.UTF_8);
        when(ai.forward(any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok().body(new byte[0]));

        mvc.perform(post("/api/ai/api/games/g1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(ai).forward(eq(HttpMethod.POST), eq("/api/games/g1/chat"),
                eq(MediaType.APPLICATION_JSON), eq(body));
    }

    @Test
    void 막힌_경로와_큰_본문은_AI를_부르지_않는다() throws Exception {
        mvc.perform(get("/api/ai/api/backend/quiz/questions/1001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AI_ENDPOINT_NOT_FOUND"));
        mvc.perform(post("/api/ai/api/quiz").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/ai/api/games/g1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new byte[AiProxyPaths.MAX_BODY_BYTES + 1]))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        verifyNoInteractions(ai);
    }
}
