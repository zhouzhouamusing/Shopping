package com.shopping.controller;

import com.shopping.dto.CouponExchangeRequest;
import com.shopping.dto.Result;
import com.shopping.entity.CategoryPointsRule;
import com.shopping.entity.PointsCoupon;
import com.shopping.entity.PointsTransaction;
import com.shopping.entity.UserCoupon;
import com.shopping.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @GetMapping("/balance")
    public Result<Integer> getBalance(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return pointsService.getBalance(userId);
    }

    @GetMapping("/transactions")
    public Result<Page<PointsTransaction>> getTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return pointsService.getTransactions(userId, page, size);
    }

    @GetMapping("/coupons")
    public Result<Page<PointsCoupon>> getAvailableCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return pointsService.getAvailableCoupons(page, size);
    }

    @PostMapping("/exchange-coupon")
    public Result<Void> exchangeCoupon(Authentication authentication,
                                       @RequestBody CouponExchangeRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return pointsService.exchangeCoupon(userId, request.getCouponId());
    }

    @GetMapping("/my-coupons")
    public Result<List<UserCoupon>> getMyCoupons(
            Authentication authentication,
            @RequestParam(required = false) String status) {
        Long userId = (Long) authentication.getPrincipal();
        return pointsService.getUserCoupons(userId, status);
    }

    @GetMapping("/rules")
    public Result<List<CategoryPointsRule>> getActiveRules() {
        return pointsService.getAllPointsRules();
    }
}
