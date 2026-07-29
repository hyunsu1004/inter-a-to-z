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
}
