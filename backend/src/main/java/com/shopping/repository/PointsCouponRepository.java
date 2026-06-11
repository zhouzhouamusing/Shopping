package com.shopping.repository;

import com.shopping.entity.PointsCoupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PointsCouponRepository extends JpaRepository<PointsCoupon, Long> {

    Page<PointsCoupon> findByStatusOrderByPointsCostAsc(Integer status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pc FROM PointsCoupon pc WHERE pc.id = :id")
    Optional<PointsCoupon> findByIdForUpdate(Long id);
}
