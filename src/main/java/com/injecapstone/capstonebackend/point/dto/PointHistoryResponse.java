package com.injecapstone.capstonebackend.point.dto;

import java.time.Instant;
import java.util.List;

public record PointHistoryResponse(List<Item> items, int page, int size, boolean hasNext) {

    public record Item(int amount, Type type, Instant recordedAt) {
    }

    public enum Type {
        VIDEO_CLICK,
        VIDEO_INNING,
        WEEKLY_QUIZ,
        OTHER
    }
}
