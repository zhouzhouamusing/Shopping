package com.shopping.repository;

import com.shopping.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/**
 * 订单数据访问层
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Order> findByOrderNo(String orderNo);
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Order> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN OrderItem oi ON oi.orderId = o.id JOIN Product p ON p.id = oi.productId WHERE p.merchantId = :merchantId ORDER BY o.createdAt DESC")
    Page<Order> findByMerchantId(@Param("merchantId") Long merchantId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN OrderItem oi ON oi.orderId = o.id JOIN Product p ON p.id = oi.productId WHERE p.merchantId = :merchantId AND o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN OrderItem oi ON oi.orderId = o.id JOIN Product p ON p.id = oi.productId WHERE p.merchantId = :merchantId")
    long countByMerchantId(@Param("merchantId") Long merchantId);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN OrderItem oi ON oi.orderId = o.id JOIN Product p ON p.id = oi.productId WHERE p.merchantId = :merchantId AND o.status = :status")
    long countByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);
}
