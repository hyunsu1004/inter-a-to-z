package com.interx.onboarding.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "core_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreValue {

    @Id
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 40)
    private String icon;

    @Column(length = 500)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
