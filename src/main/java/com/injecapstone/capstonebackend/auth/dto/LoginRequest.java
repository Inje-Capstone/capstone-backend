package com.injecapstone.capstonebackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일과 비밀번호를 이용한 로그인 요청")
public record LoginRequest(

        @Schema(
                description = "가입한 이메일 주소",
                example = "rookie@example.com"
        )
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(
                description = "가입 시 설정한 비밀번호",
                example = "Test1234!"
        )
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password

) {
}