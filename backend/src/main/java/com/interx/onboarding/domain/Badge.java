package com.interx.onboarding.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 40)
    private String icon;

    @Column(name = "condition_type", length = 30)
    private String conditionType;

    @Column(name = "condition_value")
    private Integer conditionValue;
}
