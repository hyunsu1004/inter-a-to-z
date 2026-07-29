package com.interx.onboarding.controller;

import com.interx.onboarding.dto.*;
import com.interx.onboarding.security.CurrentUser;
import com.interx.onboarding.service.CoreValueService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core-values")
@RequiredArgsConstructor
public class CoreValueController {

    private final CoreValueService coreValueService;

    @GetMapping
    public List<CoreValueDto> list(HttpServletRequest request) {
        return coreValueService.listWithProgress(CurrentUser.id(request));
    }

    @GetMapping("/{id}/cards")
    public List<ValueCardDto> cards(@PathVariable Long id, HttpServletRequest request) {
        return coreValueService.getCards(id, CurrentUser.id(request));
    }

    @PostMapping("/quiz/submit")
    public QuizSubmitResponse submitQuiz(@RequestBody QuizSubmitRequest req, HttpServletRequest request) {
        return coreValueService.submitQuiz(CurrentUser.id(request), req);
    }
}
