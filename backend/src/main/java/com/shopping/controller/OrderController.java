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

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<Order> createOrder(Authentication authentication,
                                     @Valid @RequestBody OrderRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.createOrder(userId, request);
    }

    @GetMapping
    public Result<Page<Order>> getUserOrders(Authentication authentication,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.getUserOrders(userId, page, size);
    }

    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(Authentication authentication, @PathVariable String orderNo) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.getOrderDetail(userId, orderNo);
    }

    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(Authentication authentication, @PathVariable String orderNo) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.cancelOrder(userId, orderNo);
    }

    @PutMapping("/{orderNo}/pay")
    public Result<Void> payOrder(Authentication authentication, @PathVariable String orderNo) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.payOrder(userId, orderNo);
    }

    @PutMapping("/{orderNo}/confirm")
    public Result<Void> confirmReceive(Authentication authentication, @PathVariable String orderNo) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.confirmReceive(userId, orderNo);
    }

    @PutMapping("/{orderNo}/complete")
    public Result<Void> completeOrder(Authentication authentication, @PathVariable String orderNo) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.completeOrder(userId, orderNo);
    }

    @PutMapping("/{orderNo}/refund")
    public Result<Void> refundOrder(Authentication authentication, @PathVariable String orderNo) {
        Long userId = (Long) authentication.getPrincipal();
        return orderService.refundOrder(userId, orderNo);
    }
}
