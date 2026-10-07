package com.injecapstone.capstonebackend.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportedTeam {

    LG("LG 트윈스"),
    HANWHA("한화 이글스"),
    SSG("SSG 랜더스"),
    SAMSUNG("삼성 라이온즈"),
    NC("NC 다이노스"),
    KT("KT 위즈"),
    LOTTE("롯데 자이언츠"),
    KIA("KIA 타이거즈"),
    DOOSAN("두산 베어스"),
    KIWOOM("키움 히어로즈"),
    NONE("아직 없어요");

    private final String displayName;
}