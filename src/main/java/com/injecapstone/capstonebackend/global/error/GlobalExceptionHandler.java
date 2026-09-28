package com.injecapstone.capstonebackend.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직에서 발생한 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception
    ) {

        ErrorCode errorCode = exception.getErrorCode();

        ErrorResponse response = new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    // @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        ErrorResponse response = new ErrorResponse(
                ErrorCode.INVALID_INPUT.name(),
                ErrorCode.INVALID_INPUT.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // JSON 형식 오류
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException exception
    ) {

        ErrorResponse response = new ErrorResponse(
                ErrorCode.INVALID_INPUT.name(),
                "요청 본문의 형식이 올바르지 않습니다."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}