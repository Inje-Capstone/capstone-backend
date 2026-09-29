package com.injecapstone.capstonebackend.auth.service;

import com.injecapstone.capstonebackend.auth.dto.LoginRequest;
import com.injecapstone.capstonebackend.auth.dto.LoginResponse;
import com.injecapstone.capstonebackend.auth.dto.SignUpRequest;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.global.jwt.JwtTokenProvider;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Transactional
    public Long signUp(SignUpRequest request) {

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_EMAIL
            );
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_NICKNAME
            );
        }

        // 비밀번호 암호화
        String encodedPassword =
                passwordEncoder.encode(request.password());

        // 사용자 생성
        User user = User.create(
                request.email(),
                encodedPassword,
                request.nickname()
        );

        // DB 저장
        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    // 로그인
    public LoginResponse login(LoginRequest request) {

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_CREDENTIALS
                        )
                );

        // 비밀번호 검증
        if (user.getPassword() == null ||
                !passwordEncoder.matches(
                        request.password(),
                        user.getPassword()
                )) {

            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS
            );
        }

        // JWT 생성
        String accessToken =
                jwtTokenProvider.createAccessToken(user);

        return new LoginResponse(
                user.getId(),
                accessToken
        );
    }
}