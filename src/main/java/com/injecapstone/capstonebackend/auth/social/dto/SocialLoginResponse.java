package com.injecapstone.capstonebackend.auth.social.dto;

public record SocialLoginResponse(
        Long userId,
        String accessToken,
        boolean newUser
) {
}