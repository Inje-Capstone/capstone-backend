package com.injecapstone.capstonebackend.user.repository;

import com.injecapstone.capstonebackend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
    // 본인을 제외한 다른 사용자가 해당 닉네임을 사용하는지 확인
    boolean existsByNicknameAndIdNot(String nickname, Long id);
}