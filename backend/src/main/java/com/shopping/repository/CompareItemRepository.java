package com.shopping.repository;

import com.shopping.entity.CompareItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompareItemRepository extends JpaRepository<CompareItem, Long> {

    List<CompareItem> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<CompareItem> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);

    long countByUserId(Long userId);
}
