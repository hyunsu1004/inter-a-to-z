package com.interx.onboarding.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interx.onboarding.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증은 됐지만 권한이 부족한 경우(예: 일반 유저가 /api/admin/** 접근)에 대한 응답.
 * RestAuthenticationEntryPoint(401, 미인증)와 짝을 이루며, 이 핸들러는 "인증된 사용자의 권한 부족"만
 * 다룬다 — 익명 사용자의 요청은 ExceptionTranslationFilter가 AuthenticationEntryPoint 쪽으로 보낸다.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ApiError(403, "접근 권한이 없습니다."));
    }
}
