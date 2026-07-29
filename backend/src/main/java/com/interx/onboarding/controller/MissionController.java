package com.interx.onboarding.controller;

import com.interx.onboarding.dto.MissionDto;
import com.interx.onboarding.dto.MissionSubmitRequest;
import com.interx.onboarding.security.CurrentUser;
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

    @GetMapping
    public List<MissionDto> list(HttpServletRequest request) {
        return missionService.listForUser(CurrentUser.id(request));
    }

    @PostMapping("/{id}/submit")
    public MissionDto submit(@PathVariable Long id, @RequestBody MissionSubmitRequest req, HttpServletRequest request) {
        return missionService.submit(CurrentUser.id(request), id, req);
    }
}
