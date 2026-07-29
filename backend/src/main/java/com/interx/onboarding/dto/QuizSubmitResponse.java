package com.interx.onboarding.dto;

public record QuizSubmitResponse(
        boolean correct,
        int pointsEarned,
        boolean valueCompleted,
        java.util.List<String> newBadges,
        int currentStreak,
        int totalPoints
) {}
