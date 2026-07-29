package com.interx.onboarding.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "value_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValueCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "core_value_id", nullable = false)
    private Long coreValueId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 20)
    private CardType cardType;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
