package com.interx.onboarding.config;

import com.interx.onboarding.domain.Role;
import com.interx.onboarding.domain.User;
import com.interx.onboarding.domain.UserStat;
import com.interx.onboarding.repository.UserRepository;
import com.interx.onboarding.repository.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final UserStatRepository userStatRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

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
        widenChatbotLogColumns();
    }

    /**
     * 초기 배포 당시 chatbot_logs.answer/question이 VARCHAR(255)로 생성된 환경(H2 로컬 등)이 있어
     * 이후 데이터가 길어지면 "Data too long" 오류가 났다. ddl-auto:update는 기존 컬럼 타입을
     * 넓혀주지 않으므로, 매 부팅 시 TEXT로 안전하게 재적용한다. MySQL/H2(MySQL 호환모드) 모두
     * 동일 문법을 지원하며, 이미 TEXT인 경우에도 재실행에 문제가 없다. 실패해도 앱 구동에는
     * 영향이 없도록 예외를 흡수한다.
     */
    private void widenChatbotLogColumns() {
        try {
            jdbcTemplate.execute("ALTER TABLE chatbot_logs MODIFY COLUMN question TEXT NOT NULL");
            jdbcTemplate.execute("ALTER TABLE chatbot_logs MODIFY COLUMN answer TEXT NOT NULL");
        } catch (Exception e) {
            log.warn("chatbot_logs 컬럼 타입 확장 스킵: {}", e.getMessage());
        }
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
