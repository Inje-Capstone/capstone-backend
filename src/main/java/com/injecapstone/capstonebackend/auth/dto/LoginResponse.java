package com.injecapstone.capstonebackend.auth.dto;

public record LoginResponse(
        Long userId,
        String accessToken
) {
}