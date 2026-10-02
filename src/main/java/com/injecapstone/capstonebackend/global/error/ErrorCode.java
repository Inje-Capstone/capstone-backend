package com.injecapstone.capstonebackend.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "이미 사용 중인 이메일입니다."
    ),

    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT,
            "이미 사용 중인 닉네임입니다."
    ),

    SOCIAL_ACCOUNT_LINK_REQUIRED(
            HttpStatus.CONFLICT,
            "동일한 이메일의 기존 계정이 존재합니다. 계정 연결이 필요합니다."
    ),

    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    INVALID_OAUTH_LOGIN_CODE(
            HttpStatus.BAD_REQUEST,
            "유효하지 않거나 만료된 로그인 코드입니다."
    ),

    INVALID_INPUT(
            HttpStatus.BAD_REQUEST,
            "입력값이 올바르지 않습니다."
    );

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}