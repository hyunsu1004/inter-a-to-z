package com.interx.onboarding.repository;

import com.interx.onboarding.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderByCreatedAtDesc();
    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);
}
