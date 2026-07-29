package com.interx.onboarding.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interx.onboarding.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 진짜 원인: SecurityConfig의 `.anyRequest().authenticated()`는 익명 인증(AnonymousAuthenticationToken)을
 * 인증된 것으로 보지 않는다 (AuthenticatedAuthorizationManager가 trustResolver.isAnonymous()로 명시적으로 제외).
 * 그래서 토큰이 없거나 만료된 요청은 컨트롤러(CurrentUser.id())에 도달하기도 전에
 * AuthorizationFilter 단계에서 걸러지고, ExceptionTranslationFilter가 이를 "인증 필요"로 판단해
 * AuthenticationEntryPoint를 호출한다. 이 클래스를 등록하기 전에는 Spring Security 기본값인
 * Http403ForbiddenEntryPoint가 바디 없는 403을 응답했고, 프론트엔드의 401 기반 자동 리프레시
 * 인터셉터가 전혀 동작하지 않았다 (실제 재현된 버그).
 * GlobalExceptionHandler(@RestControllerAdvice)는 DispatcherServlet 이후 컨트롤러 예외만 잡으므로
 * 이 필터 단계 응답에는 관여하지 않는다 — 그래서 별도의 AuthenticationEntryPoint가 필요하다.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ApiError(401, "인증이 필요합니다."));
    }
}
