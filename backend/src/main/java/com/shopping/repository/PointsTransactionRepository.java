package com.shopping.repository;

import com.shopping.entity.PointsTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {

    Page<PointsTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<PointsTransaction> findByOrderNoAndType(String orderNo, String type);
}
