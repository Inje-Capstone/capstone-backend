package com.injecapstone.capstonebackend.auth.social.dto;

import com.injecapstone.capstonebackend.user.domain.User;

public record SocialLoginResult(
        User user,
        boolean newUser
) {
}