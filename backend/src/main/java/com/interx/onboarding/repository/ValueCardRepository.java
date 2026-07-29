package com.interx.onboarding.repository;

import com.interx.onboarding.domain.ValueCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValueCardRepository extends JpaRepository<ValueCard, Long> {
    List<ValueCard> findByCoreValueIdOrderBySortOrderAsc(Long coreValueId);
}
