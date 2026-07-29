package com.interx.onboarding.repository;

import com.interx.onboarding.domain.CoreValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoreValueRepository extends JpaRepository<CoreValue, Long> {
    List<CoreValue> findAllByOrderBySortOrderAsc();
}
