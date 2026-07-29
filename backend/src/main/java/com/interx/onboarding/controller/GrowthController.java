package com.interx.onboarding.controller;

import com.interx.onboarding.dto.GrowthResponse;
import com.interx.onboarding.security.CurrentUser;
import com.interx.onboarding.service.GrowthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/growth")
@RequiredArgsConstructor
public class GrowthController {

    private final GrowthService growthService;

    @GetMapping
    public GrowthResponse growth(HttpServletRequest request) {
        return growthService.getGrowth(CurrentUser.id(request));
    }
}
