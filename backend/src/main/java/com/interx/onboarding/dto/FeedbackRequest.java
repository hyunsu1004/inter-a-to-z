package com.interx.onboarding.dto;

public record FeedbackRequest(
        Long missionId,
        String content
) {}
