package com.shopping.controller;

import com.shopping.dto.ProductRequest;
import com.shopping.dto.PromotionRequest;
import com.shopping.dto.ReviewReplyRequest;
import com.shopping.dto.CategoryPointsRuleRequest;
import com.shopping.dto.MemberLevelRequest;
import com.shopping.dto.MerchantApplicationReviewRequest;
import com.shopping.dto.PointsAdjustRequest;
import com.shopping.dto.PointsCouponRequest;
import com.shopping.dto.Result;
import com.shopping.entity.Order;
import com.shopping.entity.Product;
import com.shopping.entity.Promotion;
import com.shopping.entity.Review;
import com.shopping.entity.User;
import com.shopping.entity.UserMembership;
import com.shopping.entity.MemberLevel;
import com.shopping.entity.MerchantApplication;
import com.shopping.entity.CategoryPointsRule;
import com.shopping.entity.PointsCoupon;
import com.shopping.repository.MemberLevelRepository;
import com.shopping.repository.PromotionRepository;
import com.shopping.repository.UserMembershipRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.MerchantApplicationService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    private final MerchantApplicationService merchantApplicationService;
    private final UserRepository userRepository;
    private final UserMembershipRepository membershipRepository;
    private final MemberLevelRepository memberLevelRepository;
    private final PromotionRepository promotionRepository;

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

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public Result<Page<User>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String keyword) {
        Page<User> users;
        if (keyword != null && !keyword.isBlank()) {
            users = userRepository.findByUsernameContainingOrNicknameContainingOrPhoneContaining(
                    keyword, keyword, keyword, PageRequest.of(page, size));
        } else {
            users = userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        }
        // Clear password field for security
        users.getContent().forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @GetMapping("/users/{id}")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null);

        UserMembership membership = membershipRepository.findByUserId(id).orElse(null);
        MemberLevel level = null;
        if (membership != null) {
            level = memberLevelRepository.findById(membership.getLevelId()).orElse(null);
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("user", user);
        detail.put("membership", membership);
        detail.put("level", level);
        return Result.success(detail);
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            return Result.error(400, "不能禁用管理员账号");
        }
        user.setStatus(status);
        userRepository.save(user);
        return Result.success();
    }

    // ==================== 促销活动管理 ====================

    @GetMapping("/promotions")
    public Result<Page<Promotion>> getPromotions(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(promotionRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)));
    }

    @PostMapping("/promotions")
    public Result<Promotion> createPromotion(@RequestBody PromotionRequest request) {
        Promotion promotion = new Promotion();
        promotion.setName(request.getName());
        promotion.setType(request.getType());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setMinOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : java.math.BigDecimal.ZERO);
        promotion.setStartTime(request.getStartTime());
        promotion.setEndTime(request.getEndTime());
        promotion.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        promotion.setProductIds(request.getProductIds());
        promotionRepository.save(promotion);
        return Result.success(promotion);
    }

    @PutMapping("/promotions/{id}")
    public Result<Promotion> updatePromotion(@PathVariable Long id, @RequestBody PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id).orElse(null);
        if (promotion == null) {
            return Result.error(404, "促销活动不存在");
        }
        if (request.getName() != null) promotion.setName(request.getName());
        if (request.getType() != null) promotion.setType(request.getType());
        if (request.getDescription() != null) promotion.setDescription(request.getDescription());
        if (request.getDiscountValue() != null) promotion.setDiscountValue(request.getDiscountValue());
        if (request.getMinOrderAmount() != null) promotion.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getStartTime() != null) promotion.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) promotion.setEndTime(request.getEndTime());
        if (request.getStatus() != null) promotion.setStatus(request.getStatus());
        if (request.getProductIds() != null) promotion.setProductIds(request.getProductIds());
        promotionRepository.save(promotion);
        return Result.success(promotion);
    }

    @DeleteMapping("/promotions/{id}")
    public Result<Void> deletePromotion(@PathVariable Long id) {
        promotionRepository.deleteById(id);
        return Result.success();
    }

    // ==================== 商家入驻审批 ====================

    @GetMapping("/merchant-applications")
    public Result<Page<MerchantApplication>> getMerchantApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return merchantApplicationService.getAllApplications(page, size, status);
    }

    @PutMapping("/merchant-applications/{id}/review")
    public Result<Void> reviewMerchantApplication(@PathVariable Long id,
                                                   @RequestBody MerchantApplicationReviewRequest request,
                                                   Authentication authentication) {
        Long reviewerId = (Long) authentication.getPrincipal();
        return merchantApplicationService.reviewApplication(id, request, reviewerId);
    }
}
