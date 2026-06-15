package com.shopping.repository;

import com.shopping.entity.MerchantApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MerchantApplicationRepository extends JpaRepository<MerchantApplication, Long> {

    Optional<MerchantApplication> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndStatusIn(Long userId, List<String> statuses);

    Page<MerchantApplication> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<MerchantApplication> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
