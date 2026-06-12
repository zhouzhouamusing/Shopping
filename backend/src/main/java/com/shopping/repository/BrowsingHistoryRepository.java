package com.shopping.repository;

import com.shopping.entity.BrowsingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistory, Long> {

    Page<BrowsingHistory> findByUserIdOrderByBrowsedAtDesc(Long userId, Pageable pageable);

    Optional<BrowsingHistory> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);

    long countByUserId(Long userId);

    List<BrowsingHistory> findByUserIdOrderByBrowsedAtAsc(Long userId);
}
