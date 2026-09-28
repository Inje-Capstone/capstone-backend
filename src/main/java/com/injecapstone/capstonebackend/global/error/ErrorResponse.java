package com.injecapstone.capstonebackend.global.error;

public record ErrorResponse(
        String code,
        String message
) {
}