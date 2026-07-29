package com.interx.onboarding.repository;

import com.interx.onboarding.domain.ChatbotLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotLogRepository extends JpaRepository<ChatbotLog, Long> {
}
