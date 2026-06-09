package com.shopping.service;

import com.shopping.dto.CartRequest;
import com.shopping.dto.Result;
import com.shopping.entity.CartItem;
import com.shopping.entity.Product;
import com.shopping.repository.CartItemRepository;
import com.shopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 购物车服务 - 处理购物车增删改查
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    /**
     * 获取用户购物车列表
     */
    public Result<List<CartItem>> getCartList(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return Result.success(items);
    }

    /**
     * 添加商品到购物车
     */
    @Transactional
    public Result<CartItem> addToCart(Long userId, CartRequest request) {
        // 检查商品是否存在
        Product product = productRepository.findById(request.getProductId()).orElse(null);
        if (product == null) {
            return Result.error(404, "商品不存在");
        }
        if (product.getStatus() != 1) {
            return Result.error(400, "商品已下架");
        }

        // 检查购物车中是否已有该商品
        CartItem existingItem = cartItemRepository
                .findByUserIdAndProductId(userId, request.getProductId())
                .orElse(null);

        if (existingItem != null) {
            // 已存在则增加数量
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            if (existingItem.getQuantity() > product.getStock()) {
                return Result.error(400, "库存不足");
            }
            cartItemRepository.save(existingItem);
            return Result.success(existingItem);
        } else {
            // 不存在则新增
            if (request.getQuantity() > product.getStock()) {
                return Result.error(400, "库存不足");
            }
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setSelected(1);
            cartItemRepository.save(cartItem);
            return Result.success(cartItem);
        }
    }

    /**
     * 更新购物车商品数量
     */
    @Transactional
    public Result<CartItem> updateQuantity(Long userId, Long productId, Integer quantity) {
        CartItem cartItem = cartItemRepository
                .findByUserIdAndProductId(userId, productId)
                .orElse(null);
        if (cartItem == null) {
            return Result.error(404, "购物车中无此商品");
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product != null && quantity > product.getStock()) {
            return Result.error(400, "库存不足");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return Result.success(cartItem);
    }

    /**
     * 删除购物车商品
     */
    @Transactional
    public Result<Void> removeFromCart(Long userId, Long productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        return Result.success();
    }

    /**
     * 切换选中状态
     */
    @Transactional
    public Result<Void> toggleSelect(Long userId, Long productId, Integer selected) {
        CartItem cartItem = cartItemRepository
                .findByUserIdAndProductId(userId, productId)
                .orElse(null);
        if (cartItem == null) {
            return Result.error(404, "购物车中无此商品");
        }
        cartItem.setSelected(selected);
        cartItemRepository.save(cartItem);
        return Result.success();
    }

    /**
     * 全选/全不选
     */
    @Transactional
    public Result<Void> selectAll(Long userId, Integer selected) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        items.forEach(item -> item.setSelected(selected));
        cartItemRepository.saveAll(items);
        return Result.success();
    }

    /**
     * 获取购物车数量
     */
    public Result<Long> getCartCount(Long userId) {
        return Result.success(cartItemRepository.countByUserId(userId));
    }
}
