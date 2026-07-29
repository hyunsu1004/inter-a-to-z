package com.interx.onboarding.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    private String description;

    @Column(name = "related_core_value_id")
    private Long relatedCoreValueId;

    @Column(name = "created_by")
    private Long createdBy;
}
