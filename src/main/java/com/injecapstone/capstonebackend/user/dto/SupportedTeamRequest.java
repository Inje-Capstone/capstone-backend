package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.SupportedTeam;
import jakarta.validation.constraints.NotNull;

public record SupportedTeamRequest(

        @NotNull(message = "응원팀 또는 '아직 없어요'를 선택해주세요.")
        SupportedTeam supportedTeam

) {
}