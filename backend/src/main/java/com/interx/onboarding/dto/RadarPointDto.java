package com.interx.onboarding.dto;

public record RadarPointDto(
        Long coreValueId,
        String name,
        String icon,
        int score
) {}
