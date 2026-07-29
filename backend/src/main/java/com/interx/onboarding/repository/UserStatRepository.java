package com.interx.onboarding.repository;

import com.interx.onboarding.domain.UserStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatRepository extends JpaRepository<UserStat, Long> {
}
