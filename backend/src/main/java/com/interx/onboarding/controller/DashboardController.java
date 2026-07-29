package com.interx.onboarding.controller;

import com.interx.onboarding.domain.User;
import com.interx.onboarding.domain.UserStat;
import com.interx.onboarding.dto.CoreValueDto;
import com.interx.onboarding.dto.DashboardResponse;
import com.interx.onboarding.dto.MissionDto;
import com.interx.onboarding.repository.UserBadgeRepository;
import com.interx.onboarding.repository.UserRepository;
import com.interx.onboarding.security.CurrentUser;
import com.interx.onboarding.service.CoreValueService;
import com.interx.onboarding.service.GamificationService;
import com.interx.onboarding.service.MissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final GamificationService gamificationService;
    private final CoreValueService coreValueService;
    private final MissionService missionService;
    private final UserBadgeRepository userBadgeRepository;

    @GetMapping
    public DashboardResponse dashboard(HttpServletRequest request) {
        Long userId = CurrentUser.id(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        UserStat stat = gamificationService.getOrCreateStat(userId);
        List<CoreValueDto> values = coreValueService.listWithProgress(userId);
        List<MissionDto> missions = missionService.listForUser(userId);
        MissionDto currentMission = missions.stream()
                .filter(m -> !"APPROVED".equals(m.status()))
                .findFirst()
                .orElse(missions.isEmpty() ? null : missions.get(0));
        int badgeCount = userBadgeRepository.findByUserId(userId).size();

        return new DashboardResponse(
                user.getName(),
                stat.getCurrentStreak(),
                stat.getTotalPoints(),
                badgeCount,
                currentMission,
                values
        );
    }
}
