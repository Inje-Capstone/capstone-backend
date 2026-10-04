package com.injecapstone.capstonebackend.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "LOOKIE API",
                version = "v1",
                description = """
                        LOOKIE 백엔드 API 문서입니다.

                        회원가입, 로그인, OAuth 인증 및
                        서비스 기능 API를 확인하고 테스트할 수 있습니다.
                        """
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "로그인 후 발급받은 JWT Access Token을 입력합니다."
)
public class OpenApiConfig {
}