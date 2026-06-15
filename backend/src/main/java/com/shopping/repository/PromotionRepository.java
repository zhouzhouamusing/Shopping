package com.shopping.repository;

import com.shopping.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Page<Promotion> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);
    Page<Promotion> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
