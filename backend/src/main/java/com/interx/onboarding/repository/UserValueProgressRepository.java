package com.interx.onboarding.repository;

import com.interx.onboarding.domain.UserValueProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserValueProgressRepository extends JpaRepository<UserValueProgress, Long> {
    List<UserValueProgress> findByUserId(Long userId);
    Optional<UserValueProgress> findByUserIdAndCoreValueId(Long userId, Long coreValueId);
}
