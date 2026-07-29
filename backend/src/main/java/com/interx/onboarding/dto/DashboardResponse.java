package com.interx.onboarding.dto;

import java.util.List;

public record DashboardResponse(
        String name,
        int currentStreak,
        int totalPoints,
        int badgeCount,
        MissionDto currentMission,
        List<CoreValueDto> coreValues
) {}
