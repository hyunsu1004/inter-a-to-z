package com.interx.onboarding.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(
            "test-secret-key-for-unit-tests-please-32bytes",
            3600000L,   // access: 1h
            1209600000L // refresh: 14d
    );

    @Test
    void generateToken_containsExpectedClaims() {
        String token = jwtUtil.generateToken(42L, "user@interx.io", "EMPLOYEE");
        Claims claims = jwtUtil.parse(token);

        assertThat(claims.getSubject()).isEqualTo("user@interx.io");
        assertThat(claims.get("userId", String.class)).isEqualTo("42");
        assertThat(claims.get("role", String.class)).isEqualTo("EMPLOYEE");
        assertThat(claims.get("type", String.class)).isEqualTo("access");
    }

    @Test
    void generateRefreshToken_isMarkedAsRefreshType() {
        String refreshToken = jwtUtil.generateRefreshToken(7L, "admin@interx.io", "ADMIN");
        Claims claims = jwtUtil.parse(refreshToken);

        assertThat(jwtUtil.isRefreshToken(claims)).isTrue();
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void accessToken_isNotTreatedAsRefreshToken() {
        String token = jwtUtil.generateToken(1L, "a@b.com", "EMPLOYEE");
        Claims claims = jwtUtil.parse(token);

        assertThat(jwtUtil.isRefreshToken(claims)).isFalse();
    }

    @Test
    void expiredToken_failsToParse() {
        // 만료 시간을 음수로 주면 즉시 만료된 토큰이 발급됨
        JwtUtil shortLivedUtil = new JwtUtil(
                "test-secret-key-for-unit-tests-please-32bytes",
                -1000L,
                1209600000L
        );
        String expiredToken = shortLivedUtil.generateToken(1L, "a@b.com", "EMPLOYEE");

        assertThatThrownBy(() -> jwtUtil.parse(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
