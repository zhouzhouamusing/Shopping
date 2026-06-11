package com.shopping.repository;

import com.shopping.entity.PointsCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsCouponRepository extends JpaRepository<PointsCoupon, Long> {

    Page<PointsCoupon> findByStatusOrderByPointsCostAsc(Integer status, Pageable pageable);
}
