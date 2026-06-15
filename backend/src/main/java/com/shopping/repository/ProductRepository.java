package com.shopping.repository;

import com.shopping.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatus(Integer status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 1 AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 1 ORDER BY p.sales DESC")
    Page<Product> findHotProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 1 ORDER BY p.createdAt DESC")
    Page<Product> findNewProducts(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    Page<Product> findByMerchantIdOrderByCreatedAtDesc(Long merchantId, Pageable pageable);

    Optional<Product> findByIdAndMerchantId(Long id, Long merchantId);

    long countByMerchantId(Long merchantId);
}
