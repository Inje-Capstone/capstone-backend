package com.injecapstone.capstonebackend.quiz.repository;

import com.injecapstone.capstonebackend.quiz.domain.ViewedTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ViewedTermRepository extends JpaRepository<ViewedTerm, Long> {

    // 그날 본 용어 키 (처음 본 순)
    @Query("""
            SELECT v.termId
            FROM ViewedTerm v
            WHERE v.user.id = :userId
              AND v.viewedDate = :viewedDate
            ORDER BY v.id
            """)
    List<String> findTermIds(
            @Param("userId") Long userId,
            @Param("viewedDate") LocalDate viewedDate
    );
}
