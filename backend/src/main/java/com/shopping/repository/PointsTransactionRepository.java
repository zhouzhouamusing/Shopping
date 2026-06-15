package com.shopping.repository;

import com.shopping.entity.PointsTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {

    Page<PointsTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<PointsTransaction> findByOrderNoAndType(String orderNo, String type);

    List<PointsTransaction> findByTypeAndExpiredFalseAndExpireTimeBeforeOrderByExpireTimeAsc(String type, LocalDateTime time);

    @Query("SELECT pt FROM PointsTransaction pt WHERE pt.userId = :userId AND pt.type = 'EARN' " +
            "AND pt.expired = false AND pt.remainingPoints > 0 " +
            "ORDER BY pt.expireTime ASC NULLS LAST, pt.createdAt ASC")
    List<PointsTransaction> findConsumableByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointsTransaction pt " +
            "WHERE pt.userId = :userId AND pt.type = 'EARN' AND pt.expired = false " +
            "AND (pt.expireTime IS NULL OR pt.expireTime > :now)")
    int sumValidEarnedPoints(Long userId, LocalDateTime now);
}
