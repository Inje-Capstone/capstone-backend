package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.SupportedTeam;

public record SupportedTeamResponse(
        SupportedTeam code,
        String displayName
) {

    // 응원팀 enum을 선택 목록 응답으로 변환
    public static SupportedTeamResponse from(SupportedTeam team) {
        return new SupportedTeamResponse(
                team,
                team.getDisplayName()
        );
    }
}