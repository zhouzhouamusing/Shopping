package com.shopping.repository;

import com.shopping.entity.OrderEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    List<OrderEvent> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);

    List<OrderEvent> findByStatusOrderByCreatedAtAsc(String status);

    long countByStatus(String status);
}
