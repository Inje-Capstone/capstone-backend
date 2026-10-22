package com.injecapstone.capstonebackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequest(

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(
                min = 2,
                max = 20,
                message = "닉네임은 2자 이상 20자 이하로 입력해주세요."
        )
        String nickname

) {

        // DTO 생성 시 앞뒤 공백을 제거한 뒤 검증하도록 처리
        public NicknameUpdateRequest {
                if (nickname != null) {
                        nickname = nickname.strip();
                }
        }
}