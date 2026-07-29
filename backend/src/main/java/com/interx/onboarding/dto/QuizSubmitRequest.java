package com.interx.onboarding.dto;

public record QuizSubmitRequest(
        Long cardId,
        Long selectedOptionId
) {}
