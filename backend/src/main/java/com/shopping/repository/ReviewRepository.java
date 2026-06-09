package com.shopping.repository;

import com.shopping.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndStatus(Long productId, String status, Pageable pageable);

    Page<Review> findByProductIdAndStatusAndRating(Long productId, String status, Integer rating, Pageable pageable);

    Page<Review> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, String status, Pageable pageable);

    Page<Review> findByProductIdAndStatusAndRatingOrderByCreatedAtDesc(Long productId, String status, Integer rating, Pageable pageable);

    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByOrderItemId(Long orderItemId);

    Page<Review> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED'")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED'")
    Long getApprovedCountByProductId(@Param("productId") Long productId);
}
