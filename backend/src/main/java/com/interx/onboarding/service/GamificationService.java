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

    @Transactional
    public List<String> checkAndAwardBadges(Long userId) {
        List<String> newlyEarned = new ArrayList<>();
        UserStat stat = getOrCreateStat(userId);
        long completedValues = userValueProgressRepository.findByUserId(userId).stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();

        for (Badge badge : badgeRepository.findAll()) {
            if (userBadgeRepository.findByUserIdAndBadgeId(userId, badge.getId()).isPresent()) continue;
            boolean earned = evaluateBadge(badge, userId, stat, completedValues);
            if (earned) {
                userBadgeRepository.save(UserBadge.builder()
                        .userId(userId).badgeId(badge.getId()).earnedAt(LocalDateTime.now()).build());
                newlyEarned.add(badge.getName());
            }
        }
        return newlyEarned;
    }

    private boolean evaluateBadge(Badge badge, Long userId, UserStat stat, long completedValues) {
        if (badge.getConditionType() == null) return false;
        switch (badge.getConditionType()) {
            case "STREAK":
                return badge.getConditionValue() != null && stat.getCurrentStreak() >= badge.getConditionValue();
            case "VALUE_COMPLETE":
                if (badge.getConditionValue() == null) return false;
                return userValueProgressRepository
                        .findByUserIdAndCoreValueId(userId, badge.getConditionValue().longValue())
                        .map(p -> p.getStatus() == ProgressStatus.COMPLETED)
                        .orElse(false);
            case "ALL_VALUES":
                return completedValues >= 12;
            default:
                return false;
        }
    }
}
