package com.shopping.controller;

import com.shopping.dto.Result;
import com.shopping.entity.Favorite;
import com.shopping.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    public Result<Map<String, Boolean>> toggleFavorite(Authentication authentication,
                                                       @RequestParam Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.toggleFavorite(userId, productId);
    }

    @GetMapping
    public Result<Page<Favorite>> getFavorites(Authentication authentication,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.getFavorites(userId, page, size);
    }

    @DeleteMapping("/{productId}")
    public Result<Void> removeFavorite(Authentication authentication,
                                       @PathVariable Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.removeFavorite(userId, productId);
    }

    @GetMapping("/check")
    public Result<Map<String, Boolean>> checkFavorited(Authentication authentication,
                                                       @RequestParam Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.isFavorited(userId, productId);
    }

    @GetMapping("/count")
    public Result<Long> getFavoriteCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.getFavoriteCount(userId);
    }

    @GetMapping("/ids")
    public Result<List<Long>> getFavoriteIds(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.getFavoriteProductIds(userId);
    }

    @DeleteMapping("/batch")
    public Result<Void> batchRemoveFavorites(Authentication authentication,
                                             @RequestBody List<Long> productIds) {
        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.batchRemoveFavorites(userId, productIds);
    }
}
