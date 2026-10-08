package com.injecapstone.capstonebackend.user.service;

import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.dto.MyProfileResponse;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // JWT로 인증된 사용자 ID를 기준으로 본인의 정보를 조회
    public MyProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        return MyProfileResponse.from(user);
    }
}