package com.injecapstone.capstonebackend.auth.service;

import com.injecapstone.capstonebackend.auth.dto.SignUpRequest;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.injecapstone.capstonebackend.auth.dto.LoginRequest;
import com.injecapstone.capstonebackend.auth.dto.LoginResponse;
import com.injecapstone.capstonebackend.global.jwt.JwtTokenProvider;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signUp(SignUpRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());

        User user = User.create(
                request.email(),
                encodedPassword,
                request.nickname()
        );

        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.")
                );

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);

        return new LoginResponse(
                user.getId(),
                accessToken
        );
    }
}