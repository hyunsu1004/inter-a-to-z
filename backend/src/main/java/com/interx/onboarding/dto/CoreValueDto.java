package com.interx.onboarding.dto;

public record CoreValueDto(
        Long id,
        String name,
        String icon,
        String description,
        Integer sortOrder,
        String progressStatus
) {}
