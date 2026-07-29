package com.interx.onboarding.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mission_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_mission_id", nullable = false)
    private Long userMissionId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    // 조회할 때마다 User를 join하지 않도록 작성 시점의 이름/역할을 그대로 저장한다
    // (이전에 배지 N+1 문제를 겪은 경험 때문에, 코멘트 목록 조회가 인원수만큼 추가 쿼리를 날리는
    // 구조를 처음부터 피했다).
    @Column(name = "author_name", nullable = false, length = 50)
    private String authorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_role", nullable = false, length = 20)
    private Role authorRole;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
