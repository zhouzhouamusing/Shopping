package com.shopping.service;

import com.shopping.dto.Result;
import com.shopping.entity.Favorite;
import com.shopping.repository.FavoriteRepository;
import com.shopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Result<Map<String, Boolean>> toggleFavorite(Long userId, Long productId) {
        if (!productRepository.existsById(productId)) {
            return Result.error(404, "商品不存在");
        }

        Map<String, Boolean> data = new HashMap<>();
        var existing = favoriteRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            data.put("favorited", false);
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            favoriteRepository.save(favorite);
            data.put("favorited", true);
        }
        return Result.success(data);
    }

    public Result<Page<Favorite>> getFavorites(Long userId, int page, int size) {
        Page<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size));
        return Result.success(favorites);
    }

    @Transactional
    public Result<Void> removeFavorite(Long userId, Long productId) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
        return Result.success();
    }

    public Result<Map<String, Boolean>> isFavorited(Long userId, Long productId) {
        Map<String, Boolean> data = new HashMap<>();
        data.put("favorited", favoriteRepository.existsByUserIdAndProductId(userId, productId));
        return Result.success(data);
    }

    public Result<Long> getFavoriteCount(Long userId) {
        return Result.success(favoriteRepository.countByUserId(userId));
    }
}
