package com.interx.onboarding.dto;

public record AdminUserSummaryDto(
        Long userId,
        String name,
        String email,
        String department,
        int completedValues,
        int totalPoints,
        int currentStreak,
        int pendingMissions
) {}
