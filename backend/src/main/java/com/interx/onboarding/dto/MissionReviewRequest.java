package com.interx.onboarding.dto;

public record MissionReviewRequest(
        boolean approved,
        String feedback
) {}
