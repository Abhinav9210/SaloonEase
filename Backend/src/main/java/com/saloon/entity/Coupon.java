package com.saloon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private Double discountPercent;

    private Double maxDiscountAmount;
    private Double minBookingAmount;

    @Builder.Default
    private Integer maxUses = 100;

    @Builder.Default
    private Integer usedCount = 0;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id")
    private Salon salon;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
