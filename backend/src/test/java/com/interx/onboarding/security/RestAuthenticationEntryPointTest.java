package com.interx.onboarding.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 토큰 없음/만료 요청은 AuthorizationFilter 단계에서 걸러져 컨트롤러(GlobalExceptionHandler)에
 * 도달하지 못한다. 이 진입점이 Spring Security 기본값(바디 없는 403) 대신 401 + JSON을 반환하는지
 * 검증한다 — 프론트엔드 자동 리프레시 인터셉터가 이 응답 형식에 의존한다.
 */
class RestAuthenticationEntryPointTest {

    @Test
    void commence_writes401WithJsonBody() throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        entryPoint.commence(request, response, new InsufficientAuthenticationException("인증이 필요합니다."));

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        assertThat(sw.toString()).contains("\"status\":401").contains("인증이 필요합니다");
    }
}
