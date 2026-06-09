package com.shopping.controller;

import com.shopping.dto.ProductRequest;
import com.shopping.dto.Result;
import com.shopping.entity.Order;
import com.shopping.entity.Product;
import com.shopping.service.OrderService;
import com.shopping.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理控制器 - 商品管理、订单管理（需ADMIN角色）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;

    // ==================== 商品管理 ====================

    /**
     * 获取所有商品（分页）
     */
    @GetMapping("/products")
    public Result<Page<Product>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return productService.getAllProducts(page, size);
    }

    /**
     * 添加商品
     */
    @PostMapping("/products")
    public Result<Product> addProduct(@Valid @RequestBody ProductRequest request) {
        return productService.addProduct(request);
    }

    /**
     * 更新商品
     */
    @PutMapping("/products/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }

    // ==================== 订单管理 ====================

    /**
     * 获取所有订单
     */
    @GetMapping("/orders")
    public Result<Page<Order>> getAllOrders(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Integer status) {
        return orderService.getAllOrders(page, size, status);
    }

    /**
     * 发货
     */
    @PutMapping("/orders/{orderNo}/deliver")
    public Result<Void> deliverOrder(@PathVariable String orderNo) {
        return orderService.deliverOrder(orderNo);
    }

    @PutMapping("/orders/{orderNo}/refund")
    public Result<Void> refundOrder(@PathVariable String orderNo) {
        return orderService.adminRefundOrder(orderNo);
    }
}
