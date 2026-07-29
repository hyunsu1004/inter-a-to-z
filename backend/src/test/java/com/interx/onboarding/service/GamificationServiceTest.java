package com.interx.onboarding.service;

import com.interx.onboarding.domain.*;
import com.interx.onboarding.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * checkAndAwardBadges()의 N+1 쿼리 제거 리팩터링이 기존 배지 지급 로직(STREAK / VALUE_COMPLETE /
 * ALL_VALUES 조건, 이미 획득한 배지 재지급 방지)을 그대로 보존하는지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock private UserStatRepository userStatRepository;
    @Mock private PointsLogRepository pointsLogRepository;
    @Mock private BadgeRepository badgeRepository;
    @Mock private UserBadgeRepository userBadgeRepository;
    @Mock private UserValueProgressRepository userValueProgressRepository;

    @InjectMocks
    private GamificationService gamificationService;

    private UserStat stat;

    @BeforeEach
    void setUp() {
        stat = UserStat.builder().userId(1L).currentStreak(7).longestStreak(7).totalPoints(50).build();
        lenient().when(userStatRepository.findById(1L)).thenReturn(Optional.of(stat));
    }

    @Test
    void streakBadge_isAwardedWhenStreakMeetsThreshold() {
        Badge streakBadge = Badge.builder().id(10L).name("7일 연속").conditionType("STREAK").conditionValue(7).build();
        when(badgeRepository.findAll()).thenReturn(List.of(streakBadge));
        when(userBadgeRepository.findByUserId(1L)).thenReturn(List.of());
        when(userValueProgressRepository.findByUserId(1L)).thenReturn(List.of());

        List<String> earned = gamificationService.checkAndAwardBadges(1L);

        assertThat(earned).containsExactly("7일 연속");
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void valueCompleteBadge_isAwardedOnlyWhenThatSpecificValueIsCompleted() {
        Badge valueBadge = Badge.builder().id(11L).name("집요한 끈기 마스터").conditionType("VALUE_COMPLETE").conditionValue(3).build();
        UserValueProgress completed = UserValueProgress.builder()
                .id(1L).userId(1L).coreValueId(3L).status(ProgressStatus.COMPLETED).build();
        UserValueProgress inProgress = UserValueProgress.builder()
                .id(2L).userId(1L).coreValueId(4L).status(ProgressStatus.IN_PROGRESS).build();

        when(badgeRepository.findAll()).thenReturn(List.of(valueBadge));
        when(userBadgeRepository.findByUserId(1L)).thenReturn(List.of());
        when(userValueProgressRepository.findByUserId(1L)).thenReturn(List.of(completed, inProgress));

        List<String> earned = gamificationService.checkAndAwardBadges(1L);

        assertThat(earned).containsExactly("집요한 끈기 마스터");
    }

    @Test
    void alreadyEarnedBadge_isNeverAwardedAgain() {
        Badge streakBadge = Badge.builder().id(10L).name("7일 연속").conditionType("STREAK").conditionValue(7).build();
        UserBadge existing = UserBadge.builder().id(1L).userId(1L).badgeId(10L).earnedAt(LocalDateTime.now()).build();

        when(badgeRepository.findAll()).thenReturn(List.of(streakBadge));
        when(userBadgeRepository.findByUserId(1L)).thenReturn(List.of(existing));
        when(userValueProgressRepository.findByUserId(1L)).thenReturn(List.of());

        List<String> earned = gamificationService.checkAndAwardBadges(1L);

        assertThat(earned).isEmpty();
        verify(userBadgeRepository, never()).save(any(UserBadge.class));
    }

    @Test
    void allValuesBadge_isAwardedWhenAllTwelveValuesCompleted() {
        Badge allValuesBadge = Badge.builder().id(12L).name("올클리어").conditionType("ALL_VALUES").conditionValue(null).build();
        List<UserValueProgress> twelveCompleted = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(i -> UserValueProgress.builder()
                        .id((long) i).userId(1L).coreValueId((long) i).status(ProgressStatus.COMPLETED).build())
                .toList();

        when(badgeRepository.findAll()).thenReturn(List.of(allValuesBadge));
        when(userBadgeRepository.findByUserId(1L)).thenReturn(List.of());
        when(userValueProgressRepository.findByUserId(1L)).thenReturn(twelveCompleted);

        List<String> earned = gamificationService.checkAndAwardBadges(1L);

        assertThat(earned).containsExactly("올클리어");
    }

    @Test
    void checkAndAwardBadges_queriesEachRepositoryExactlyOnce_regardlessOfBadgeCount() {
        List<Badge> manyBadges = java.util.stream.IntStream.rangeClosed(1, 15)
                .mapToObj(i -> Badge.builder().id((long) i).name("배지" + i).conditionType("STREAK").conditionValue(999).build())
                .toList();

        when(badgeRepository.findAll()).thenReturn(manyBadges);
        when(userBadgeRepository.findByUserId(1L)).thenReturn(List.of());
        when(userValueProgressRepository.findByUserId(1L)).thenReturn(List.of());

        gamificationService.checkAndAwardBadges(1L);

        // N+1이었다면 배지 수(15)만큼 호출됐겠지만, 리팩터링 후에는 배지 개수와 무관하게 1회만 호출돼야 한다.
        verify(userBadgeRepository, times(1)).findByUserId(1L);
        verify(userValueProgressRepository, times(1)).findByUserId(1L);
    }
}
