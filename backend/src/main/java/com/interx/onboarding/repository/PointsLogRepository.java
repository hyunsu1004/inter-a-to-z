package com.interx.onboarding.repository;

import com.interx.onboarding.domain.PointsLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointsLogRepository extends JpaRepository<PointsLog, Long> {
    List<PointsLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
