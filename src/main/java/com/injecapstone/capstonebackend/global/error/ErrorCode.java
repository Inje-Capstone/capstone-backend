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
    DIAGNOSIS_NOT_COMPLETED(
            HttpStatus.BAD_REQUEST,
            "수준 진단을 먼저 완료해주세요."
    ),

    SUPPORTED_TEAM_NOT_SELECTED(
            HttpStatus.BAD_REQUEST,
            "응원팀 또는 '아직 없어요'를 선택해주세요."
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "사용자를 찾을 수 없습니다."
    ),

    REQUIRED_TERMS_NOT_AGREED(
            HttpStatus.BAD_REQUEST,
            "필수 약관에 먼저 동의해주세요."
    ),

    INVALID_INPUT(
            HttpStatus.BAD_REQUEST,
            "입력값이 올바르지 않습니다."
    ),

    AI_ENDPOINT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "지원하지 않는 AI 경로입니다."
    ),

    AI_SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "AI 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해주세요."
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