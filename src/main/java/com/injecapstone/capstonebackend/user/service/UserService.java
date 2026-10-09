package com.injecapstone.capstonebackend.user.service;

import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.dto.MyProfileResponse;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // JWT로 인증된 사용자의 기본 정보와 학습 설정 조회
    public MyProfileResponse getMyProfile(Long userId) {
        return MyProfileResponse.from(findUser(userId));
    }

    // 본인의 닉네임을 변경하고 변경된 내 정보 반환
    @Transactional
    public MyProfileResponse updateNickname(
            Long userId,
            String nickname
    ) {
        User user = findUser(userId);

        // 현재 닉네임과 같으면 변경 없이 정상 응답
        if (user.getNickname().equals(nickname)) {
            return MyProfileResponse.from(user);
        }

        // 다른 사용자의 닉네임과 중복되는지 확인
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.changeNickname(nickname);

        try {
            // DB에 즉시 반영하여 중복 제약 위반 확인
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            // 동시 요청으로 발생한 닉네임 중복도 동일한 오류로 처리
            if (isNicknameConstraintViolation(exception)) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }

            throw exception;
        }

        return MyProfileResponse.from(user);
    }

    // 사용자 조회: 존재하지 않으면 USER_NOT_FOUND 예외 발생
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );
    }

    // DB 오류의 원인 중 닉네임 고유 제약 위반이 있는지 확인
    private boolean isNicknameConstraintViolation(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();

                if (constraintName != null
                        && constraintName.contains("uk_users_nickname")) {
                    return true;
                }
            }

            cause = cause.getCause();
        }

        return false;
    }
}