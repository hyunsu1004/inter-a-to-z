package com.interx.onboarding.dto;

public record AuthResponse(
        String token,
        String refreshToken,
        Long userId,
        String name,
        String email,
        String role
) {}
