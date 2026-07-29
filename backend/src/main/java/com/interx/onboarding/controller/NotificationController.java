package com.interx.onboarding.controller;

import com.interx.onboarding.security.CurrentUser;
import com.interx.onboarding.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 브라우저 기본 EventSource는 커스텀 헤더(Authorization)를 지정할 수 없어서, 토큰을 쿼리
 * 파라미터로 노출하는 방식이 흔히 쓰인다. 이 프로젝트는 로그인 잠금/토큰 로테이션 등 인증 보안에
 * 신경써온 만큼 그 트레이드오프를 피하고, 프론트엔드에서 fetch + ReadableStream으로 SSE를 직접
 * 파싱해 기존 Authorization 헤더 방식을 그대로 유지한다 (frontend/src/context/NotificationContext.jsx).
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(HttpServletRequest request) {
        return notificationService.subscribe(CurrentUser.id(request));
    }
}
