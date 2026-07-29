package com.interx.onboarding.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

public final class CurrentUser {

    private CurrentUser() {}

    public static Long id(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return (Long) userId;
    }
}
