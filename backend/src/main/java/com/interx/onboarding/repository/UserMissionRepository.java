package com.interx.onboarding.repository;

import com.interx.onboarding.domain.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    List<UserMission> findByUserId(Long userId);
    Optional<UserMission> findByUserIdAndMissionId(Long userId, Long missionId);
}
