package com.interx.onboarding.service;

import com.interx.onboarding.domain.Role;
import com.interx.onboarding.domain.User;
import com.interx.onboarding.domain.UserStat;
import com.interx.onboarding.dto.AuthResponse;
import com.interx.onboarding.dto.LoginRequest;
import com.interx.onboarding.dto.RefreshRequest;
import com.interx.onboarding.dto.SignupRequest;
import com.interx.onboarding.repository.UserRepository;
import com.interx.onboarding.repository.UserStatRepository;
import com.interx.onboarding.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserStatRepository userStatRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        User user = User.builder()
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .name(req.name())
                .department(req.department())
                .role(Role.EMPLOYEE)
                .build();
        user = userRepository.save(user);

        UserStat stat = UserStat.builder()
                .userId(user.getId())
                .currentStreak(0)
                .longestStreak(0)
                .lastActiveDate(null)
                .totalPoints(0)
                .build();
        userStatRepository.save(stat);

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return issueTokens(user);
    }

    /**
     * 액세스 토큰(짧은 만료)이 만료되어도, 리프레시 토큰(긴 만료)이 유효하면 재로그인 없이
     * 새 액세스 토큰을 발급한다. 리프레시 토큰도 함께 새로 발급(로테이션)해 탈취 위험을 줄인다.
     */
    public AuthResponse refresh(RefreshRequest req) {
        Claims claims;
        try {
            claims = jwtUtil.parse(req.refreshToken());
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("유효하지 않거나 만료된 리프레시 토큰입니다.");
        }
        if (!jwtUtil.isRefreshToken(claims)) {
            throw new BadCredentialsException("리프레시 토큰이 아닙니다.");
        }
        Long userId = Long.parseLong(claims.get("userId", String.class));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("존재하지 않는 사용자입니다."));
        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, refreshToken, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
