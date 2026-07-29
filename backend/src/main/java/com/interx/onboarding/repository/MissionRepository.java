package com.interx.onboarding.repository;

import com.interx.onboarding.domain.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findAllByOrderByWeekNumberAsc();
}
