package com.interx.onboarding.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 로그인 브루트포스 방지를 위한 계정 잠금 로직.
 *
 * 별도 인프라(Redis 등) 없이도 동작하도록 인메모리(ConcurrentHashMap)로 이메일별 실패 횟수를 추적한다.
 * 단일 레플리카로 운영 중이라 인스턴스 간 상태 공유 문제는 없지만, 레플리카를 늘리면 인메모리 방식의
 * 한계(인스턴스마다 카운트가 따로 집계됨)가 생긴다는 점은 감안해야 한다 — 그 경우 Redis 등 공유 저장소로
 * 옮기는 것이 다음 단계.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(5);

    private static class AttemptInfo {
        int failureCount = 0;
        Instant lockedUntil = null;
    }

    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    /** 현재 잠겨있으면 true. 잠금 시간이 지났다면 자동으로 풀어준다. */
    public boolean isLocked(String email) {
        AttemptInfo info = attempts.get(normalize(email));
        if (info == null || info.lockedUntil == null) return false;
        if (Instant.now().isAfter(info.lockedUntil)) {
            attempts.remove(normalize(email));
            return false;
        }
        return true;
    }

    /** 잠금이 언제 풀리는지 남은 초 단위로 반환 (잠겨있지 않으면 0). */
    public long remainingLockSeconds(String email) {
        AttemptInfo info = attempts.get(normalize(email));
        if (info == null || info.lockedUntil == null) return 0;
        long remaining = Duration.between(Instant.now(), info.lockedUntil).getSeconds();
        return Math.max(remaining, 0);
    }

    public void recordFailure(String email) {
        AttemptInfo info = attempts.computeIfAbsent(normalize(email), k -> new AttemptInfo());
        info.failureCount++;
        if (info.failureCount >= MAX_ATTEMPTS) {
            info.lockedUntil = Instant.now().plus(LOCKOUT_DURATION);
        }
    }

    public void recordSuccess(String email) {
        attempts.remove(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
