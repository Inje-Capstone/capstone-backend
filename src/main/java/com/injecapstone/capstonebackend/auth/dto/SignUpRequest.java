package com.injecapstone.capstonebackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignUpRequest(

        @Schema(
                description = "가입할 이메일 주소",
                example = "rookie@example.com"
        )
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(
                description = "비밀번호. 8자 이상 20자 이하",
                example = "Test1234!"
        )
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        String password,

        @Schema(
                description = "닉네임. 2자 이상 20자 이하",
                example = "루키"
        )
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
        String nickname

) {
        // 회원가입 닉네임의 앞뒤 공백을 제거한 뒤 검증
        public SignUpRequest {
                if (nickname != null) {
                        nickname = nickname.strip();
                }
        }
}