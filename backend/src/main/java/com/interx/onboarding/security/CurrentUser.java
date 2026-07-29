package com.interx.onboarding.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.InsufficientAuthenticationException;

public final class CurrentUser {

    private CurrentUser() {}

    public static Long id(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            // 토큰이 없거나 만료/위조돼 JwtAuthFilter가 userId를 채우지 못한 경우.
            // "권한 부족"(403)이 아니라 "인증 자체가 안 됨"(401)이 맞는 상태 코드다 —
            // 프론트엔드의 자동 리프레시 인터셉터가 401을 기준으로 동작하므로, 여기서 403을 던지면
            // 액세스 토큰 만료 시 재발급 없이 화면이 조용히 멈추는 문제로 이어진다.
            throw new InsufficientAuthenticationException("인증이 필요합니다.");
        }
        return (Long) userId;
    }
}
