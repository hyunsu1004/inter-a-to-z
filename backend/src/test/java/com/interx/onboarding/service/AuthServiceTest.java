package com.interx.onboarding.service;

import com.interx.onboarding.domain.Role;
import com.interx.onboarding.domain.User;
import com.interx.onboarding.dto.AuthResponse;
import com.interx.onboarding.dto.LoginRequest;
import com.interx.onboarding.dto.RefreshRequest;
import com.interx.onboarding.dto.SignupRequest;
import com.interx.onboarding.repository.UserRepository;
import com.interx.onboarding.repository.UserStatRepository;
import com.interx.onboarding.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 이번에 새로 추가한 리프레시 토큰 플로우가 핵심 검증 대상.
 * (액세스 토큰이 만료돼도 유효한 리프레시 토큰으로 재로그인 없이 재발급되는지,
 *  리프레시 토큰을 액세스 토큰 자리에 잘못 써도 거부되는지)
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserStatRepository userStatRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    // 순수 로직이라 목(mock) 대신 실제 인스턴스를 사용해 토큰 발급/파싱까지 실제로 검증한다.
    private final JwtUtil jwtUtil = new JwtUtil(
            "test-secret-key-for-unit-tests-please-32bytes",
            3600000L,
            1209600000L
    );

    // 브루트포스 잠금 로직도 순수 로직이라 실제 인스턴스를 사용한다.
    private final LoginAttemptService loginAttemptService = new LoginAttemptService();

    private AuthService authService;
    private User existingUser;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, userStatRepository, passwordEncoder, jwtUtil, loginAttemptService);
        existingUser = User.builder()
                .id(10L)
                .email("newbie@interx.io")
                .password("encoded-password")
                .name("심사위원 데모")
                .role(Role.EMPLOYEE)
                .build();
    }

    @Test
    void login_withCorrectPassword_returnsAccessAndRefreshTokens() {
        when(userRepository.findByEmail("newbie@interx.io")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("demo1234!", "encoded-password")).thenReturn(true);

        AuthResponse response = authService.login(new LoginRequest("newbie@interx.io", "demo1234!"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.token()).isNotEqualTo(response.refreshToken());
        assertThat(response.userId()).isEqualTo(10L);
    }

    @Test
    void login_withWrongPassword_throwsBadCredentials() {
        when(userRepository.findByEmail("newbie@interx.io")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("newbie@interx.io", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refresh_withValidRefreshToken_issuesNewAccessToken() {
        String refreshToken = jwtUtil.generateRefreshToken(10L, existingUser.getEmail(), "EMPLOYEE");
        when(userRepository.findById(10L)).thenReturn(Optional.of(existingUser));

        AuthResponse response = authService.refresh(new RefreshRequest(refreshToken));

        assertThat(response.token()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        // 로테이션: 새 리프레시 토큰은 이전 것과 달라야 한다
        assertThat(response.refreshToken()).isNotEqualTo(refreshToken);
    }

    @Test
    void refresh_withAccessTokenInsteadOfRefreshToken_isRejected() {
        String accessToken = jwtUtil.generateToken(10L, existingUser.getEmail(), "EMPLOYEE");

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(accessToken)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refresh_forDeletedUser_isRejected() {
        String refreshToken = jwtUtil.generateRefreshToken(999L, "ghost@interx.io", "EMPLOYEE");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(refreshToken)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refresh_withGarbageToken_isRejected() {
        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("not-a-real-jwt")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void signup_newEmail_createsUserAndReturnsTokens() {
        when(userRepository.existsByEmail("new@interx.io")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(55L);
            return u;
        });

        AuthResponse response = authService.signup(
                new SignupRequest("new@interx.io", "pw1234!", "새 직원", "AX Intern"));

        assertThat(response.userId()).isEqualTo(55L);
        assertThat(response.token()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
    }

    @Test
    void signup_duplicateEmail_throwsIllegalArgument() {
        when(userRepository.existsByEmail("newbie@interx.io")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(
                new SignupRequest("newbie@interx.io", "pw1234!", "누구", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_fiveConsecutiveFailures_locksAccount() {
        when(userRepository.findByEmail("newbie@interx.io")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> authService.login(new LoginRequest("newbie@interx.io", "wrong")))
                    .isInstanceOf(BadCredentialsException.class);
        }

        // 6번째 시도는 비밀번호를 맞게 넣어도 잠금 때문에(비밀번호 검증까지 가지 않고) 거부돼야 한다
        assertThatThrownBy(() -> authService.login(new LoginRequest("newbie@interx.io", "demo1234!")))
                .isInstanceOf(LockedException.class);
    }

    @Test
    void login_successfulLogin_resetsFailureCount() {
        when(userRepository.findByEmail("newbie@interx.io")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);
        when(passwordEncoder.matches("demo1234!", "encoded-password")).thenReturn(true);

        // 4번 실패 (5번째부터 잠기므로 아직 안전)
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> authService.login(new LoginRequest("newbie@interx.io", "wrong")))
                    .isInstanceOf(BadCredentialsException.class);
        }
        // 성공하면 카운트가 초기화된다
        assertThat(authService.login(new LoginRequest("newbie@interx.io", "demo1234!")).token()).isNotBlank();

        // 다시 4번 실패해도 (누적 8번이 아니라 리셋 후 4번이므로) 아직 잠기지 않아야 한다
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> authService.login(new LoginRequest("newbie@interx.io", "wrong")))
                    .isInstanceOf(BadCredentialsException.class);
        }
        assertThat(authService.login(new LoginRequest("newbie@interx.io", "demo1234!")).token()).isNotBlank();
    }
}
