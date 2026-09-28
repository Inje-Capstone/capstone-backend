package com.injecapstone.capstonebackend.global.config;

import com.injecapstone.capstonebackend.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


        @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .exceptionHandling(exception -> exception
                        // 인증 정보가 없거나 인증에 실패한 경우
                        .authenticationEntryPoint((request, response, authException) ->
                                response.setStatus(
                                        HttpServletResponse.SC_UNAUTHORIZED
                                )
                        )

                        // 인증은 되었지만 접근 권한이 없는 경우
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.setStatus(
                                        HttpServletResponse.SC_FORBIDDEN
                                )
                        )
                )

                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/error").permitAll()
                                .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}