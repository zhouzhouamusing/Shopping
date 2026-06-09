package com.shopping.repository;

import com.shopping.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentNo(String paymentNo);

    Optional<Payment> findByOrderNoAndPaymentStatus(String orderNo, String paymentStatus);

    Optional<Payment> findByOrderNo(String orderNo);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByPaymentStatusAndExpireTimeBefore(String paymentStatus, LocalDateTime time);
}
