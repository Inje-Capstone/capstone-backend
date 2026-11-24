package com.injecapstone.capstonebackend.point.repository;

import com.injecapstone.capstonebackend.point.domain.PointTransaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface PointTransactionRepository extends Repository<PointTransaction, Long> {

    <S extends PointTransaction> S save(S transaction);
    boolean existsByUser_IdAndEventKey(Long userId, String eventKey);

    @Query("""
            select sum(pt.amount)
            from PointTransaction pt
            where pt.user.id = :userId
            """)
    Long sumAmountByUserId(@Param("userId") Long userId);
}