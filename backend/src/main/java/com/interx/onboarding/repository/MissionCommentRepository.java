package com.interx.onboarding.repository;

import com.interx.onboarding.domain.MissionComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionCommentRepository extends JpaRepository<MissionComment, Long> {
    List<MissionComment> findByUserMissionIdOrderByCreatedAtAsc(Long userMissionId);
}
