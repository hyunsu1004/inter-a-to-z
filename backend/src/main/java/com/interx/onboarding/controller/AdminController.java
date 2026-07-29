package com.interx.onboarding.controller;

import com.interx.onboarding.domain.Feedback;
import com.interx.onboarding.domain.Mission;
import com.interx.onboarding.domain.UserMission;
import com.interx.onboarding.dto.AdminUserSummaryDto;
import com.interx.onboarding.dto.FeedbackReplyRequest;
import com.interx.onboarding.dto.MissionCommentDto;
import com.interx.onboarding.dto.MissionCommentRequest;
import com.interx.onboarding.dto.MissionReviewRequest;
import com.interx.onboarding.repository.FeedbackRepository;
import com.interx.onboarding.repository.MissionRepository;
import com.interx.onboarding.repository.UserMissionRepository;
import com.interx.onboarding.security.CurrentUser;
import com.interx.onboarding.service.AdminService;
import com.interx.onboarding.service.MissionCommentService;
import com.interx.onboarding.service.MissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final MissionService missionService;
    private final MissionCommentService missionCommentService;
    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final FeedbackRepository feedbackRepository;

    @GetMapping("/users")
    public List<AdminUserSummaryDto> users() {
        return adminService.listUserSummaries();
    }

    @GetMapping("/missions")
    public List<Mission> missions() {
        return missionRepository.findAllByOrderByWeekNumberAsc();
    }

    @PostMapping("/missions")
    public Mission createMission(@RequestBody Mission mission) {
        mission.setId(null);
        return missionRepository.save(mission);
    }

    @DeleteMapping("/missions/{id}")
    public void deleteMission(@PathVariable Long id) {
        missionRepository.deleteById(id);
    }

    @GetMapping("/missions/submissions")
    public List<UserMission> submissions() {
        return userMissionRepository.findAll();
    }

    @PostMapping("/missions/submissions/{userMissionId}/review")
    public UserMission review(@PathVariable Long userMissionId, @RequestBody MissionReviewRequest req) {
        return missionService.review(userMissionId, req.approved(), req.feedback());
    }

    @GetMapping("/missions/submissions/{userMissionId}/comments")
    public List<MissionCommentDto> comments(@PathVariable Long userMissionId) {
        return missionCommentService.listForAdmin(userMissionId);
    }

    @PostMapping("/missions/submissions/{userMissionId}/comments")
    public MissionCommentDto addComment(@PathVariable Long userMissionId,
                                         @RequestBody MissionCommentRequest req,
                                         HttpServletRequest request) {
        return missionCommentService.addAsAdmin(CurrentUser.id(request), userMissionId, req.content());
    }

    @GetMapping("/feedbacks")
    public List<Feedback> feedbacks() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping("/feedbacks/{id}/reply")
    public Feedback reply(@PathVariable Long id, @RequestBody FeedbackReplyRequest req) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 피드백입니다."));
        feedback.setReply(req.reply());
        return feedbackRepository.save(feedback);
    }
}
