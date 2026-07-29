package com.interx.onboarding.dto;

import java.util.List;

public record GrowthResponse(
        List<RadarPointDto> radar,
        List<TimelineEntryDto> timeline
) {}
