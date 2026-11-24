package com.injecapstone.capstonebackend.point.service;

import com.injecapstone.capstonebackend.point.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointTransactionRepository pointTransactionRepository;

    @Transactional(readOnly = true)
    public long getBalance(Long userId) {
        Long balance = pointTransactionRepository.sumAmountByUserId(userId);
        return balance == null ? 0L : balance;
    }
}