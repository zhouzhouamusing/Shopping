package com.shopping.repository;

import com.shopping.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByMerchantId(Long merchantId);
}
