package com.shopping.controller;

import com.shopping.dto.OrderRequest;
import com.shopping.dto.Result;
import com.shopping.entity.Order;
import com.shopping.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器 - 订单创建、查询、操作（需登录）
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public Result<Order> createOrder(Authentication authentication,
                                     @Valid @RequestBody OrderRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.createOrder(userId, request);
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping
    public Result<Page<Order>> getUserOrders(Authentication authentication,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.getUserOrders(userId, page, size);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(Authentication authentication, @PathVariable String orderNo) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.getOrderDetail(userId, orderNo);
    }

    /**
     * 取消订单
     */
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(Authentication authentication, @PathVariable String orderNo) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.cancelOrder(userId, orderNo);
    }

    /**
     * 模拟支付
     */
    @PutMapping("/{orderNo}/pay")
    public Result<Void> payOrder(Authentication authentication, @PathVariable String orderNo) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.payOrder(userId, orderNo);
    }
}
