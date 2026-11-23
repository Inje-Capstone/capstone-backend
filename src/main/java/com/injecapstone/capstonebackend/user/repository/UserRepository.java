package com.injecapstone.capstonebackend.user.repository;

import com.injecapstone.capstonebackend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select u from User u
            where u.email = :email and u.password is not null
            """)
    Optional<User> findByEmailForPasswordReset(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select u from User u
            where u.id = :userId and u.password is not null
            """)
    Optional<User> findByIdForPasswordReset(@Param("userId") Long userId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
    // 본인을 제외한 다른 사용자가 해당 닉네임을 사용하는지 확인
    boolean existsByNicknameAndIdNot(String nickname, Long id);
}