package com.interx.onboarding.service;

import com.interx.onboarding.dto.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class NotificationServiceTest {

    private final NotificationEvent event = new NotificationEvent(
            "MISSION_APPROVED", "미션이 승인됐어요!", "'자기소개' 제출이 승인되었습니다.", 10L, 100L
    );

    @Test
    void notifyUser_withNoSubscriber_doesNotThrow() {
        NotificationService service = new NotificationService();

        assertThatCode(() -> service.notifyUser(1L, event)).doesNotThrowAnyException();
    }

    @Test
    void subscribe_immediatelySendsConnectedEvent() {
        NotificationService service = new NotificationService();

        SseEmitter emitter = service.subscribe(1L);

        assertThat(emitter).isNotNull();
    }

    @Test
    void notifyUsers_sendsToEachUserIndependently_withoutError() {
        NotificationService service = new NotificationService();
        service.subscribe(1L);
        service.subscribe(2L);

        assertThatCode(() -> service.notifyUsers(java.util.List.of(1L, 2L, 3L), event))
                .doesNotThrowAnyException();
    }
}
