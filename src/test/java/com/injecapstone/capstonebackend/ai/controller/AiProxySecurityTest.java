package com.injecapstone.capstonebackend.ai.controller;

import com.injecapstone.capstonebackend.ai.client.AiClient;
import com.injecapstone.capstonebackend.auth.social.handler.OAuth2AuthenticationFailureHandler;
import com.injecapstone.capstonebackend.auth.social.handler.OAuth2AuthenticationSuccessHandler;
import com.injecapstone.capstonebackend.global.config.CorsConfig;
import com.injecapstone.capstonebackend.global.config.SecurityConfig;
import com.injecapstone.capstonebackend.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// /api/ai/** 중계는 로그인(Bearer JWT)이 있어야 AI로 넘어간다 — 실제 SecurityConfig로 확인
@WebMvcTest(controllers = AiProxyController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test",
        "spring.security.oauth2.client.registration.google.client-secret=test",
        "spring.security.oauth2.client.registration.kakao.client-id=test",
        "spring.security.oauth2.client.registration.kakao.client-secret=test"
})
class AiProxySecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AiClient aiClient;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private OAuth2AuthenticationSuccessHandler successHandler;

    @MockitoBean
    private OAuth2AuthenticationFailureHandler failureHandler;

    @Test
    void 토큰이_없거나_틀리면_401이고_AI를_부르지_않는다() throws Exception {
        when(jwtTokenProvider.validateToken("bad")).thenReturn(false);

        mvc.perform(get("/api/ai/api/games"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/ai/api/games").header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(aiClient);
    }

    @Test
    void 로그인한_사용자는_AI로_중계된다() throws Exception {
        when(jwtTokenProvider.validateToken("good")).thenReturn(true);
        when(jwtTokenProvider.getUserId("good")).thenReturn(7L);
        when(jwtTokenProvider.getRole("good")).thenReturn("USER");
        when(aiClient.forward(eq(HttpMethod.GET), eq("/api/games"), any(), any()))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[]".getBytes(StandardCharsets.UTF_8)));

        mvc.perform(get("/api/ai/api/games").header("Authorization", "Bearer good"))
                .andExpect(status().isOk());
    }
}
