package com.shopping.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "points_coupons")
public class PointsCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "points_cost", nullable = false)
    private Integer pointsCost;

    @Column(name = "coupon_type", nullable = false, length = 20)
    private String couponType;

    @Column(name = "coupon_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal couponValue;

    @Column(name = "min_order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "valid_days", nullable = false)
    private Integer validDays = 30;

    @Column(name = "total_stock", nullable = false)
    private Integer totalStock = -1;

    @Column(name = "remaining_stock", nullable = false)
    private Integer remainingStock = -1;

    @Column(nullable = false)
    private Integer status = 1;

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
