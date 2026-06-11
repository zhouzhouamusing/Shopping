package com.shopping.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_membership")
public class UserMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "level_id", nullable = false)
    private Long levelId = 1L;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Column(name = "total_earned_points", nullable = false)
    private Integer totalEarnedPoints = 0;

    @Column(name = "total_spending", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSpending = BigDecimal.ZERO;

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
