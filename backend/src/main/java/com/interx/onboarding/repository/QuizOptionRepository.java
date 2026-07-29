package com.interx.onboarding.repository;

import com.interx.onboarding.domain.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
    List<QuizOption> findByCardId(Long cardId);
}
