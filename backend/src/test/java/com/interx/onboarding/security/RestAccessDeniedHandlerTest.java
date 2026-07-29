package com.interx.onboarding.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 인증은 됐지만 권한이 부족한 경우(예: 일반 유저의 /api/admin/** 접근)는 여전히 403 + JSON을
 * 반환해야 한다 — RestAuthenticationEntryPoint(401, 미인증)와 명확히 구분된다.
 */
class RestAccessDeniedHandlerTest {

    @Test
    void handle_writes403WithJsonBody() throws Exception {
        RestAccessDeniedHandler handler = new RestAccessDeniedHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        handler.handle(request, response, new AccessDeniedException("접근 권한이 없습니다."));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        assertThat(sw.toString()).contains("\"status\":403").contains("접근 권한이 없습니다");
    }
}
