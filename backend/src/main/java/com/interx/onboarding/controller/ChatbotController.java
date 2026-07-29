package com.interx.onboarding.controller;

import com.interx.onboarding.dto.ChatbotRequest;
import com.interx.onboarding.dto.ChatbotResponse;
import com.interx.onboarding.security.CurrentUser;
import com.interx.onboarding.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ChatbotResponse ask(@RequestBody ChatbotRequest req, HttpServletRequest request) {
        String answer = chatbotService.ask(CurrentUser.id(request), req.message());
        return new ChatbotResponse(answer);
    }

    /** 사용자가 묻지 않아도 학습 현황을 근거로 먼저 말을 거는 개인화 코칭 메시지. */
    @GetMapping("/coaching")
    public ChatbotResponse coaching(HttpServletRequest request) {
        String answer = chatbotService.coachingMessage(CurrentUser.id(request));
        return new ChatbotResponse(answer);
    }
}
