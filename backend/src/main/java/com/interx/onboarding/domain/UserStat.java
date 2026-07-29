package com.interx.onboarding.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStat {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;
}
