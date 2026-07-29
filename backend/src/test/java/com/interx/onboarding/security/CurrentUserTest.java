package com.interx.onboarding.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * JwtAuthFilter가 유효한 토큰을 못 채워줬을 때(토큰 없음/만료/위조) CurrentUser.id()가
 * 403이 아니라 401에 매핑되는 InsufficientAuthenticationException을 던지는지 검증한다.
 * 프론트엔드의 자동 토큰 재발급 로직이 401만 감지하므로, 여기서 잘못된 예외 타입을 던지면
 * 액세스 토큰 만료 시 화면이 조용히 멈추는 버그로 이어진다 (실제로 재현된 문제).
 */
class CurrentUserTest {

    @Test
    void id_returnsUserId_whenAttributePresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(42L);

        assertThat(CurrentUser.id(request)).isEqualTo(42L);
    }

    @Test
    void id_throwsInsufficientAuthentication_whenAttributeMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(null);

        assertThatThrownBy(() -> CurrentUser.id(request))
                .isInstanceOf(InsufficientAuthenticationException.class);
    }
}
