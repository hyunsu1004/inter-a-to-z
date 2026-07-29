package com.interx.onboarding.dto;

public record MissionDto(
        Long id,
        Integer weekNumber,
        String title,
        String description,
        String status,
        String submissionText,
        String feedback
) {}
