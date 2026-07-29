package com.interx.onboarding.controller;

import com.interx.onboarding.dto.MissionCommentDto;
import com.interx.onboarding.dto.MissionCommentRequest;
import com.interx.onboarding.dto.MissionDto;
import com.interx.onboarding.dto.MissionSubmitRequest;
import com.interx.onboarding.security.CurrentUser;
import com.interx.onboarding.service.MissionCommentService;
import com.interx.onboarding.service.MissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;
    private final MissionCommentService missionCommentService;

    @GetMapping
    public List<MissionDto> list(HttpServletRequest request) {
        return missionService.listForUser(CurrentUser.id(request));
    }

    @PostMapping("/{id}/submit")
    public MissionDto submit(@PathVariable Long id, @RequestBody MissionSubmitRequest req, HttpServletRequest request) {
        return missionService.submit(CurrentUser.id(request), id, req);
    }

    @GetMapping("/submissions/{userMissionId}/comments")
    public List<MissionCommentDto> comments(@PathVariable Long userMissionId, HttpServletRequest request) {
        return missionCommentService.listForEmployee(CurrentUser.id(request), userMissionId);
    }

    @PostMapping("/submissions/{userMissionId}/comments")
    public MissionCommentDto addComment(@PathVariable Long userMissionId,
                                         @RequestBody MissionCommentRequest req,
                                         HttpServletRequest request) {
        return missionCommentService.addAsEmployee(CurrentUser.id(request), userMissionId, req.content());
    }
}
