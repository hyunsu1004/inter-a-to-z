package com.interx.onboarding.service;

import com.interx.onboarding.domain.Role;
import com.interx.onboarding.domain.User;
import com.interx.onboarding.domain.UserStat;
import com.interx.onboarding.dto.AuthResponse;
import com.interx.onboarding.dto.LoginRequest;
import com.interx.onboarding.dto.SignupRequest;
import com.interx.onboarding.repository.UserRepository;
import com.interx.onboarding.repository.UserStatRepository;
import com.interx.onboarding.security.JwtUtil;
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

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
