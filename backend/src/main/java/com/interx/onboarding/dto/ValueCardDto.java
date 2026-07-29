package com.interx.onboarding.dto;

import java.util.List;

public record ValueCardDto(
        Long id,
        String cardType,
        String content,
        Integer sortOrder,
        List<QuizOptionDto> options
) {}
