package com.shopping.repository;

import com.shopping.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    List<UserCoupon> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserCoupon> findByIdAndUserId(Long id, Long userId);

    List<UserCoupon> findByStatusAndExpireTimeBefore(String status, LocalDateTime time);
}
