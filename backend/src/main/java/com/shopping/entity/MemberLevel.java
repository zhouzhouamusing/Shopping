package com.shopping.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "member_levels")
public class MemberLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "level_code", nullable = false, unique = true)
    private Integer levelCode;

    @Column(name = "min_spending", nullable = false, precision = 12, scale = 2)
    private BigDecimal minSpending = BigDecimal.ZERO;

    @Column(name = "min_points", nullable = false)
    private Integer minPoints = 0;

    @Column(name = "discount_rate", nullable = false, precision = 3, scale = 2)
    private BigDecimal discountRate = BigDecimal.ONE;

    @Column(name = "points_multiplier", nullable = false, precision = 3, scale = 2)
    private BigDecimal pointsMultiplier = BigDecimal.ONE;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String icon;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
