package com.shopping.controller;

import com.shopping.dto.ProductRequest;
import com.shopping.dto.ReviewReplyRequest;
import com.shopping.dto.CategoryPointsRuleRequest;
import com.shopping.dto.MemberLevelRequest;
import com.shopping.dto.PointsAdjustRequest;
import com.shopping.dto.PointsCouponRequest;
import com.shopping.dto.Result;
import com.shopping.entity.Order;
import com.shopping.entity.Product;
import com.shopping.entity.Review;
import com.shopping.entity.MemberLevel;
import com.shopping.entity.CategoryPointsRule;
import com.shopping.entity.PointsCoupon;
import com.shopping.service.OrderEventQueueService;
import com.shopping.service.OrderService;
import com.shopping.service.PriceCircuitBreakerService;
import com.shopping.service.ProductService;
import com.shopping.service.ReviewService;
import com.shopping.service.MembershipService;
import com.shopping.service.PointsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    private final ReviewService reviewService;
    private final PriceCircuitBreakerService priceCircuitBreaker;
    private final OrderEventQueueService orderEventQueue;
    private final MembershipService membershipService;
    private final PointsService pointsService;

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

    // ==================== 熔断器管理 ====================

    @GetMapping("/circuit-breaker/price/status")
    public Result<PriceCircuitBreakerService.CircuitBreakerStatus> getPriceCircuitBreakerStatus() {
        return Result.success(priceCircuitBreaker.getStatus());
    }

    @PutMapping("/circuit-breaker/price/reset")
    public Result<Void> resetPriceCircuitBreaker() {
        priceCircuitBreaker.reset();
        return Result.success();
    }

    // ==================== 事件队列监控 ====================

    @GetMapping("/event-queue/monitoring")
    public Result<OrderEventQueueService.MonitoringData> getEventQueueMonitoring() {
        return Result.success(orderEventQueue.getMonitoringData());
    }

    @GetMapping("/event-queue/dead-letters")
    public Result<java.util.List<OrderEventQueueService.OrderStatusEvent>> getDeadLetterEvents(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(orderEventQueue.getDeadLetterEvents(limit));
    }

    @PutMapping("/event-queue/retry-dead-letters")
    public Result<Integer> retryDeadLetterEvents() {
        int count = orderEventQueue.retryDeadLetterEvents();
        return Result.success(count);
    }

    // ==================== 评价管理 ====================

    @GetMapping("/reviews")
    public Result<Page<Review>> getAllReviews(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String status) {
        return reviewService.getAllReviews(page, size, status);
    }

    @PutMapping("/reviews/{id}/approve")
    public Result<Void> approveReview(@PathVariable Long id) {
        return reviewService.approveReview(id);
    }

    @PutMapping("/reviews/{id}/reject")
    public Result<Void> rejectReview(@PathVariable Long id) {
        return reviewService.rejectReview(id);
    }

    @PutMapping("/reviews/{id}/reply")
    public Result<Void> replyReview(@PathVariable Long id, @RequestBody ReviewReplyRequest request) {
        return reviewService.replyReview(id, request.getAdminReply());
    }

    // ==================== 会员等级管理 ====================

    @GetMapping("/member-levels")
    public Result<java.util.List<MemberLevel>> getMemberLevels() {
        return membershipService.getAllLevels();
    }

    @PostMapping("/member-levels")
    public Result<MemberLevel> createMemberLevel(@RequestBody MemberLevelRequest request) {
        return membershipService.createLevel(request);
    }

    @PutMapping("/member-levels/{id}")
    public Result<MemberLevel> updateMemberLevel(@PathVariable Long id, @RequestBody MemberLevelRequest request) {
        return membershipService.updateLevel(id, request);
    }

    @DeleteMapping("/member-levels/{id}")
    public Result<Void> deleteMemberLevel(@PathVariable Long id) {
        return membershipService.deleteLevel(id);
    }

    // ==================== 积分规则管理 ====================

    @GetMapping("/points-rules")
    public Result<java.util.List<CategoryPointsRule>> getPointsRules() {
        return pointsService.getAllPointsRulesAdmin();
    }

    @PostMapping("/points-rules")
    public Result<CategoryPointsRule> savePointsRule(@RequestBody CategoryPointsRuleRequest request) {
        return pointsService.savePointsRule(request);
    }

    @DeleteMapping("/points-rules/{id}")
    public Result<Void> deletePointsRule(@PathVariable Long id) {
        return pointsService.deletePointsRule(id);
    }

    // ==================== 积分优惠券管理 ====================

    @GetMapping("/points-coupons")
    public Result<Page<PointsCoupon>> getPointsCoupons(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return pointsService.getAllCouponsAdmin(page, size);
    }

    @PostMapping("/points-coupons")
    public Result<PointsCoupon> createPointsCoupon(@RequestBody PointsCouponRequest request) {
        return pointsService.createCoupon(request);
    }

    @PutMapping("/points-coupons/{id}")
    public Result<PointsCoupon> updatePointsCoupon(@PathVariable Long id, @RequestBody PointsCouponRequest request) {
        return pointsService.updateCoupon(id, request);
    }

    @DeleteMapping("/points-coupons/{id}")
    public Result<Void> deletePointsCoupon(@PathVariable Long id) {
        return pointsService.deleteCoupon(id);
    }

    // ==================== 积分手动调整 ====================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/points/adjust")
    public Result<Void> adjustPoints(Authentication authentication,
                                     @RequestBody PointsAdjustRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return pointsService.adjustPoints(request.getUserId(), request.getPoints(),
                request.getReason(), operatorId);
    }
}
