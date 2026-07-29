package com.interx.onboarding.dto;

/**
 * SSE로 밀어주는 실시간 알림 이벤트.
 * type: MISSION_SUBMITTED(신입->사수), MISSION_APPROVED / MISSION_REJECTED(사수->신입),
 *       NEW_COMMENT(양방향)
 */
public record NotificationEvent(
        String type,
        String title,
        String message,
        Long missionId,
        Long userMissionId
) {}
