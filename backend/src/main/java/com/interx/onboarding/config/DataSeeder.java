package com.interx.onboarding.config;

import com.interx.onboarding.domain.Role;
import com.interx.onboarding.domain.User;
import com.interx.onboarding.domain.UserStat;
import com.interx.onboarding.repository.UserRepository;
import com.interx.onboarding.repository.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserStatRepository userStatRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email}")
    private String adminEmail;
    @Value("${app.seed.admin-password}")
    private String adminPassword;
    @Value("${app.seed.demo-email}")
    private String demoEmail;
    @Value("${app.seed.demo-password}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedUser(adminEmail, adminPassword, "인사팀 관리자", Role.ADMIN, "People Team");
        seedUser(demoEmail, demoPassword, "심사위원 데모", Role.EMPLOYEE, "AX Intern");
    }

    private void seedUser(String email, String rawPassword, String name, Role role, String department) {
        if (userRepository.existsByEmail(email)) return;
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .name(name)
                .department(department)
                .role(role)
                .build();
        user = userRepository.save(user);

        userStatRepository.save(UserStat.builder()
                .userId(user.getId())
                .currentStreak(0)
                .longestStreak(0)
                .totalPoints(0)
                .build());
    }
}
