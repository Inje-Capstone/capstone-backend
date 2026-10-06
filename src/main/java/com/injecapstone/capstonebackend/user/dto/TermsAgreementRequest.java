package com.injecapstone.capstonebackend.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record TermsAgreementRequest(

        @NotNull(message = "서비스 이용약관 동의 여부는 필수입니다.")
        @AssertTrue(message = "서비스 이용약관에 동의해야 합니다.")
        Boolean termsAgreed,

        @NotNull(message = "개인정보 수집·이용 동의 여부는 필수입니다.")
        @AssertTrue(message = "개인정보 수집·이용에 동의해야 합니다.")
        Boolean privacyAgreed,

        @NotNull(message = "경기 알림 수신 동의 여부를 입력해주세요.")
        Boolean gameNotificationEnabled

) {
}