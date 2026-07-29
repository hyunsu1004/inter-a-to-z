package com.interx.onboarding.dto;

import java.time.LocalDateTime;

public record TimelineEntryDto(
        LocalDateTime occurredAt,
        String type,
        String label,
        int points
) {}
