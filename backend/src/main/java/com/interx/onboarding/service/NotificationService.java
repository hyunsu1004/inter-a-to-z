package com.interx.onboarding.service;

import com.interx.onboarding.dto.NotificationEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 사용자별 SSE(Server-Sent Events) 커넥션을 메모리에 들고 있다가, 미션 제출/승인/반려/코멘트 같은
 * 이벤트가 발생하면 바로 push한다. Railway가 1 replica로 떠 있는 이 프로젝트 규모에서는 별도의
 * 메시지 브로커(Redis pub/sub 등) 없이 인메모리 Map으로 충분하고, 새 런타임 의존성을 추가하지
 * 않는다는 이전 배포 사고(Swagger/springdoc 크래시)의 교훈도 따른 것이다.
 * 여러 replica로 스케일아웃하게 되면 이 방식은 깨지므로, 그때는 Redis pub/sub 등으로 옮겨야 한다.
 */
@Service
public class NotificationService {

    private final Map<Long, List<SseEmitter>> emittersByUserId = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(0L); // 타임아웃 없음 - 연결이 끊길 때까지 유지
        emittersByUserId.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> unregister(userId, emitter));
        emitter.onTimeout(() -> unregister(userId, emitter));
        emitter.onError(ex -> unregister(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ignored) {
            // 연결 직후 끊기는 경우는 무시 - 다음 재연결 시도로 정리됨
        }
        return emitter;
    }

    public void notifyUser(Long userId, NotificationEvent event) {
        List<SseEmitter> emitters = emittersByUserId.get(userId);
        if (emitters == null || emitters.isEmpty()) return;

        for (SseEmitter emitter : List.copyOf(emitters)) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(event));
            } catch (IOException ex) {
                unregister(userId, emitter);
            }
        }
    }

    public void notifyUsers(List<Long> userIds, NotificationEvent event) {
        userIds.forEach(id -> notifyUser(id, event));
    }

    private void unregister(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUserId.get(userId);
        if (emitters == null) return;
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUserId.remove(userId);
        }
    }
}
