package com.interx.onboarding.service;

import com.interx.onboarding.domain.*;
import com.interx.onboarding.dto.*;
import com.interx.onboarding.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final GamificationService gamificationService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<MissionDto> listForUser(Long userId) {
        return missionRepository.findAllByOrderByWeekNumberAsc().stream()
                .map(m -> toDto(m, userMissionRepository.findByUserIdAndMissionId(userId, m.getId()).orElse(null)))
                .collect(Collectors.toList());
    }

    private MissionDto toDto(Mission m, UserMission um) {
        return new MissionDto(
                m.getId(), m.getWeekNumber(), m.getTitle(), m.getDescription(),
                um == null ? "ASSIGNED" : um.getStatus().name(),
                um == null ? null : um.getSubmissionText(),
                um == null ? null : um.getFeedback(),
                um == null ? null : um.getId()
        );
    }

    @Transactional
    public MissionDto submit(Long userId, Long missionId, MissionSubmitRequest req) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 미션입니다."));
        UserMission um = userMissionRepository.findByUserIdAndMissionId(userId, missionId)
                .orElseGet(() -> UserMission.builder().userId(userId).missionId(missionId).build());
        um.setStatus(MissionStatus.SUBMITTED);
        um.setSubmissionText(req.submissionText());
        um.setSubmittedAt(LocalDateTime.now());
        userMissionRepository.save(um);

        gamificationService.recordActivity(userId);
        gamificationService.awardPoints(userId, "MISSION_SUBMIT", missionId, 20);
        gamificationService.checkAndAwardBadges(userId);

        notifySubmission(mission, um);

        return toDto(mission, um);
    }

    @Transactional
    public UserMission review(Long userMissionId, boolean approved, String feedback) {
        UserMission um = userMissionRepository.findById(userMissionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제출건입니다."));
        um.setStatus(approved ? MissionStatus.APPROVED : MissionStatus.REJECTED);
        um.setFeedback(feedback);
        userMissionRepository.save(um);
        if (approved) {
            gamificationService.awardPoints(um.getUserId(), "MISSION_APPROVED", um.getMissionId(), 10);
        }

        notifyReview(um, approved);

        return um;
    }

    private void notifySubmission(Mission mission, UserMission um) {
        List<Long> adminIds = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .map(User::getId)
                .toList();
        notificationService.notifyUsers(adminIds, new NotificationEvent(
                "MISSION_SUBMITTED",
                "새 미션 제출",
                "새 제출물이 도착했습니다: '" + mission.getTitle() + "'",
                mission.getId(), um.getId()
        ));
    }

    private void notifyReview(UserMission um, boolean approved) {
        String title = missionRepository.findById(um.getMissionId()).map(Mission::getTitle).orElse("미션");
        notificationService.notifyUser(um.getUserId(), new NotificationEvent(
                approved ? "MISSION_APPROVED" : "MISSION_REJECTED",
                approved ? "미션이 승인됐어요!" : "미션이 반려됐어요",
                "'" + title + "' 제출이 " + (approved ? "승인" : "반려") + "되었습니다.",
                um.getMissionId(), um.getId()
        ));
    }
}
