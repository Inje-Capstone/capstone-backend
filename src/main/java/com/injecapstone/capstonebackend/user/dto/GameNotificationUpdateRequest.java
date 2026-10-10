package com.injecapstone.capstonebackend.user.dto;

import jakarta.validation.constraints.NotNull;

public record GameNotificationUpdateRequest(

        @NotNull(message = "경기 알림 수신 여부는 필수입니다.")
        Boolean gameNotificationEnabled

) {
}