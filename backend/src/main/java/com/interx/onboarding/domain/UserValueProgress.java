package com.interx.onboarding.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_value_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "core_value_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserValueProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "core_value_id", nullable = false)
    private Long coreValueId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status;

    private LocalDateTime completedAt;

    private Integer quizScore;
}
