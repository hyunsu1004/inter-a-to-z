package com.interx.onboarding.service;

import com.interx.onboarding.domain.*;
import com.interx.onboarding.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final UserStatRepository userStatRepository;
    private final PointsLogRepository pointsLogRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserValueProgressRepository userValueProgressRepository;

    @Transactional
    public UserStat getOrCreateStat(Long userId) {
        return userStatRepository.findById(userId).orElseGet(() ->
                userStatRepository.save(UserStat.builder()
                        .userId(userId).currentStreak(0).longestStreak(0).totalPoints(0).build()));
    }

    @Transactional
    public UserStat recordActivity(Long userId) {
        UserStat stat = getOrCreateStat(userId);
        LocalDate today = LocalDate.now();
        LocalDate last = stat.getLastActiveDate();
        if (last == null) {
            stat.setCurrentStreak(1);
        } else if (last.equals(today)) {
            // already active today
        } else if (last.equals(today.minusDays(1))) {
            stat.setCurrentStreak(stat.getCurrentStreak() + 1);
        } else {
            stat.setCurrentStreak(1);
        }
        stat.setLastActiveDate(today);
        if (stat.getCurrentStreak() > stat.getLongestStreak()) {
            stat.setLongestStreak(stat.getCurrentStreak());
        }
        return userStatRepository.save(stat);
    }

    @Transactional
    public UserStat awardPoints(Long userId, String sourceType, Long sourceId, int points) {
        UserStat stat = getOrCreateStat(userId);
        stat.setTotalPoints(stat.getTotalPoints() + points);
        userStatRepository.save(stat);
        pointsLogRepository.save(PointsLog.builder()
                .userId(userId).sourceType(sourceType).sourceId(sourceId).points(points).build());
        return stat;
    }

    /**
     * 배지 지급 조건 평가.
     * 이전 구현은 badgeRepository.findAll()로 순회하며 배지마다
     * userBadgeRepository.findByUserIdAndBadgeId(...)와 (VALUE_COMPLETE 배지의 경우)
     * userValueProgressRepository.findByUserIdAndCoreValueId(...)를 각각 호출해,
     * 배지 개수(현재 15개)만큼 쿼리가 반복되는 N+1 패턴이었다.
     * 이미 획득한 배지 목록과 완료한 핵심가치 id 목록을 각각 한 번의 쿼리로 미리 조회해
     * Set에 담아두고 메모리에서 비교하는 방식으로 바꿔 쿼리 횟수를 badgeRepository.findAll() 포함
     * 총 3회(고정)로 줄였다. 배지/핵심가치 개수가 늘어나도 쿼리 수는 늘지 않는다.
     */
    @Transactional
    public List<String> checkAndAwardBadges(Long userId) {
        List<String> newlyEarned = new ArrayList<>();
        UserStat stat = getOrCreateStat(userId);

        List<UserValueProgress> progressList = userValueProgressRepository.findByUserId(userId);
        long completedValues = progressList.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();
        Set<Long> completedCoreValueIds = progressList.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .map(UserValueProgress::getCoreValueId)
                .collect(Collectors.toSet());

        Set<Long> alreadyEarnedBadgeIds = userBadgeRepository.findByUserId(userId).stream()
                .map(UserBadge::getBadgeId)
                .collect(Collectors.toSet());

        for (Badge badge : badgeRepository.findAll()) {
            if (alreadyEarnedBadgeIds.contains(badge.getId())) continue;
            boolean earned = evaluateBadge(badge, stat, completedValues, completedCoreValueIds);
            if (earned) {
                userBadgeRepository.save(UserBadge.builder()
                        .userId(userId).badgeId(badge.getId()).earnedAt(LocalDateTime.now()).build());
                newlyEarned.add(badge.getName());
            }
        }
        return newlyEarned;
    }

    private boolean evaluateBadge(Badge badge, UserStat stat, long completedValues, Set<Long> completedCoreValueIds) {
        if (badge.getConditionType() == null) return false;
        switch (badge.getConditionType()) {
            case "STREAK":
                return badge.getConditionValue() != null && stat.getCurrentStreak() >= badge.getConditionValue();
            case "VALUE_COMPLETE":
                return badge.getConditionValue() != null
                        && completedCoreValueIds.contains(badge.getConditionValue().longValue());
            case "ALL_VALUES":
                return completedValues >= 12;
            default:
                return false;
        }
    }
}
