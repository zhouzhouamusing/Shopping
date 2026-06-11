package com.shopping.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 */
@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "receiver_address", length = 500)
    private String receiverAddress;

    @Column(length = 500)
    private String remark;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "payment_no", length = 50)
    private String paymentNo;

    @Column(name = "points_used")
    private Integer pointsUsed = 0;

    @Column(name = "points_discount", precision = 10, scale = 2)
    private BigDecimal pointsDiscount = BigDecimal.ZERO;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "coupon_discount", precision = 10, scale = 2)
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    @Column(name = "actual_amount", precision = 10, scale = 2)
    private BigDecimal actualAmount;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    @Column(name = "finish_time")
    private LocalDateTime finishTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "orderId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

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
