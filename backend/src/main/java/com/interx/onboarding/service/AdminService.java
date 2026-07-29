package com.interx.onboarding.service;

import com.interx.onboarding.domain.*;
import com.interx.onboarding.dto.AdminUserSummaryDto;
import com.interx.onboarding.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserStatRepository userStatRepository;
    private final UserValueProgressRepository userValueProgressRepository;
    private final UserMissionRepository userMissionRepository;

    public List<AdminUserSummaryDto> listUserSummaries() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE)
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    private AdminUserSummaryDto toSummary(User u) {
        UserStat stat = userStatRepository.findById(u.getId())
                .orElse(UserStat.builder().userId(u.getId()).currentStreak(0).longestStreak(0).totalPoints(0).build());

        long completed = userValueProgressRepository.findByUserId(u.getId()).stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();

        long pending = userMissionRepository.findByUserId(u.getId()).stream()
                .filter(m -> m.getStatus() == MissionStatus.SUBMITTED)
                .count();

        return new AdminUserSummaryDto(
                u.getId(), u.getName(), u.getEmail(), u.getDepartment(),
                (int) completed, stat.getTotalPoints(), stat.getCurrentStreak(), (int) pending
        );
    }
}
