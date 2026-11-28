package com.injecapstone.capstonebackend.point.repository;

import com.injecapstone.capstonebackend.point.domain.PointTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PointTransactionRepository extends Repository<PointTransaction, Long> {

    <S extends PointTransaction> S save(S transaction);

    boolean existsByUser_IdAndEventKey(Long userId, String eventKey);

    Slice<HistoryEntry> findByUser_IdOrderByRecordedAtUtcDescIdDesc(Long userId, Pageable pageable);

    @Query("""
            select sum(pt.amount)
            from PointTransaction pt
            where pt.user.id = :userId
            """)
    Long sumAmountByUserId(@Param("userId") Long userId);

    @Query("""
        select count(pt)
        from PointTransaction pt
        where pt.user.id = :userId
          and pt.eventKey like concat(:eventPrefix, '%')
          and pt.recordedAtUtc >= :startUtc
          and pt.recordedAtUtc < :endUtc
        """)
    long countAwardsBetween(
            @Param("userId") Long userId,
            @Param("eventPrefix") String eventPrefix,
            @Param("startUtc") LocalDateTime startUtc,
            @Param("endUtc") LocalDateTime endUtc
    );

    interface HistoryEntry {
        int getAmount();
        String getEventKey();
        LocalDateTime getRecordedAtUtc();
    }
}
