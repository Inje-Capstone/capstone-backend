package com.injecapstone.capstonebackend.user.dto;

import com.injecapstone.capstonebackend.user.domain.SupportedTeam;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "응원팀 선택 목록의 항목. NONE(아직 없어요) 포함")
public record SupportedTeamResponse(
        @Schema(
                description = "응원팀 코드. 응원팀 설정 요청의 supportedTeam 값으로 사용",
                example = "NONE"
        )
        SupportedTeam code,

        @Schema(
                description = "화면에 표시할 응원팀 이름. NONE은 '아직 없어요'로 표시",
                example = "아직 없어요"
        )
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