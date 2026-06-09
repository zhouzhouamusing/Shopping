package com.shopping.controller;

import com.shopping.dto.CartRequest;
import com.shopping.dto.Result;
import com.shopping.entity.CartItem;
import com.shopping.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器 - 购物车增删改查（需登录）
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 获取购物车列表
     */
    @GetMapping
    public Result<List<CartItem>> getCartList(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return cartService.getCartList(userId);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping
    public Result<CartItem> addToCart(Authentication authentication, @Valid @RequestBody CartRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return cartService.addToCart(userId, request);
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/{productId}")
    public Result<CartItem> updateQuantity(Authentication authentication,
                                           @PathVariable Long productId,
                                           @RequestParam Integer quantity) {
        Long userId = (Long) authentication.getPrincipal();
        return cartService.updateQuantity(userId, productId, quantity);
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{productId}")
    public Result<Void> removeFromCart(Authentication authentication, @PathVariable Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        return cartService.removeFromCart(userId, productId);
    }

    /**
     * 切换选中状态
     */
    @PutMapping("/{productId}/select")
    public Result<Void> toggleSelect(Authentication authentication,
                                     @PathVariable Long productId,
                                     @RequestParam Integer selected) {
        Long userId = (Long) authentication.getPrincipal();
        return cartService.toggleSelect(userId, productId, selected);
    }

    /**
     * 全选/全不选
     */
    @PutMapping("/select-all")
    public Result<Void> selectAll(Authentication authentication, @RequestParam Integer selected) {
        Long userId = (Long) authentication.getPrincipal();
        return cartService.selectAll(userId, selected);
    }

    /**
     * 获取购物车数量
     */
    @GetMapping("/count")
    public Result<Long> getCartCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return cartService.getCartCount(userId);
    }
}
