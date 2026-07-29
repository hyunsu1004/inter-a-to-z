package com.interx.onboarding.dto;

public record BadgeDto(
        Long id,
        String name,
        String icon,
        boolean earned
) {}
