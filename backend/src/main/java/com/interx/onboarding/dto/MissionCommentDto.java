package com.interx.onboarding.dto;

import java.time.LocalDateTime;

public record MissionCommentDto(
        Long id,
        Long authorId,
        String authorName,
        String authorRole,
        String content,
        LocalDateTime createdAt
) {}
