package com.shopping.service;

import com.shopping.dto.CategoryPointsRuleRequest;
import com.shopping.dto.PointsCouponRequest;
import com.shopping.dto.Result;
import com.shopping.entity.*;
import com.shopping.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsService {

    private final UserMembershipRepository membershipRepository;
    private final PointsTransactionRepository transactionRepository;
    private final CategoryPointsRuleRepository pointsRuleRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final MemberLevelRepository memberLevelRepository;
    private final PointsCouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final MembershipService membershipService;

    private static final BigDecimal DEFAULT_POINTS_RATE = new BigDecimal("0.0100");
    private static final BigDecimal POINTS_TO_YUAN = new BigDecimal("0.01");

    @Transactional(rollbackFor = Exception.class)
    public void awardPointsForOrder(String orderNo) {
        List<PointsTransaction> existing = transactionRepository.findByOrderNoAndType(orderNo, "EARN");
        if (!existing.isEmpty()) {
            log.info("积分已发放，跳过重复发放: orderNo={}", orderNo);
            return;
        }

        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            log.error("发放积分失败，订单不存在: orderNo={}", orderNo);
            return;
        }

        UserMembership membership = membershipRepository.findByUserIdForUpdate(order.getUserId()).orElse(null);
        if (membership == null) {
            membership = membershipService.initMembership(order.getUserId());
        }

        MemberLevel level = memberLevelRepository.findById(membership.getLevelId()).orElse(null);
        BigDecimal multiplier = level != null ? level.getPointsMultiplier() : BigDecimal.ONE;

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        int totalPoints = 0;

        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            BigDecimal rate = DEFAULT_POINTS_RATE;
            if (product != null && product.getCategoryId() != null) {
                CategoryPointsRule rule = pointsRuleRepository.findByCategoryId(product.getCategoryId()).orElse(null);
                if (rule != null) {
                    rate = rule.getPointsRate();
                }
            }
            BigDecimal rawPoints = item.getTotalPrice().multiply(rate);
            totalPoints += rawPoints.intValue();
        }

        int finalPoints = BigDecimal.valueOf(totalPoints).multiply(multiplier)
                .setScale(0, RoundingMode.FLOOR).intValue();

        if (finalPoints <= 0) {
            return;
        }

        membership.setTotalPoints(membership.getTotalPoints() + finalPoints);
        membership.setTotalEarnedPoints(membership.getTotalEarnedPoints() + finalPoints);
        membership.setTotalSpending(membership.getTotalSpending().add(order.getTotalAmount()));
        membershipRepository.save(membership);

        PointsTransaction transaction = new PointsTransaction();
        transaction.setUserId(order.getUserId());
        transaction.setType("EARN");
        transaction.setPoints(finalPoints);
        transaction.setBalanceAfter(membership.getTotalPoints());
        transaction.setOrderNo(orderNo);
        transaction.setReason("订单支付奖励");
        transactionRepository.save(transaction);

        log.info("积分发放成功: userId={}, orderNo={}, points={}, balance={}",
                order.getUserId(), orderNo, finalPoints, membership.getTotalPoints());

        membershipService.checkAndUpgrade(order.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deductPointsForRefund(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) return;

        UserMembership membership = membershipRepository.findByUserIdForUpdate(order.getUserId()).orElse(null);
        if (membership == null) return;

        List<PointsTransaction> earnRecords = transactionRepository.findByOrderNoAndType(orderNo, "EARN");
        if (!earnRecords.isEmpty()) {
            int earnedPoints = earnRecords.stream().mapToInt(PointsTransaction::getPoints).sum();
            int deductPoints = Math.min(earnedPoints, membership.getTotalPoints());

            if (deductPoints > 0) {
                membership.setTotalPoints(membership.getTotalPoints() - deductPoints);
                PointsTransaction tx = new PointsTransaction();
                tx.setUserId(order.getUserId());
                tx.setType("DEDUCT");
                tx.setPoints(-deductPoints);
                tx.setBalanceAfter(membership.getTotalPoints());
                tx.setOrderNo(orderNo);
                tx.setReason("订单退款扣减");
                transactionRepository.save(tx);
            }
        }

        membership.setTotalSpending(membership.getTotalSpending().subtract(order.getTotalAmount())
                .max(BigDecimal.ZERO));
        membershipRepository.save(membership);

        if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            membership.setTotalPoints(membership.getTotalPoints() + order.getPointsUsed());
            membershipRepository.save(membership);

            PointsTransaction restoreTx = new PointsTransaction();
            restoreTx.setUserId(order.getUserId());
            restoreTx.setType("EARN");
            restoreTx.setPoints(order.getPointsUsed());
            restoreTx.setBalanceAfter(membership.getTotalPoints());
            restoreTx.setOrderNo(orderNo);
            restoreTx.setReason("退款返还抵扣积分");
            transactionRepository.save(restoreTx);
        }

        log.info("退款积分处理完成: userId={}, orderNo={}", order.getUserId(), orderNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<BigDecimal> redeemPointsForOrder(Long userId, int points, String orderNo) {
        if (points <= 0) {
            return Result.error(400, "积分数量必须大于0");
        }

        UserMembership membership = membershipRepository.findByUserIdForUpdate(userId).orElse(null);
        if (membership == null) {
            return Result.error(400, "会员信息不存在");
        }
        if (membership.getTotalPoints() < points) {
            return Result.error(400, "积分余额不足");
        }

        BigDecimal discount = BigDecimal.valueOf(points).multiply(POINTS_TO_YUAN);

        membership.setTotalPoints(membership.getTotalPoints() - points);
        membershipRepository.save(membership);

        PointsTransaction tx = new PointsTransaction();
        tx.setUserId(userId);
        tx.setType("SPEND");
        tx.setPoints(-points);
        tx.setBalanceAfter(membership.getTotalPoints());
        tx.setOrderNo(orderNo);
        tx.setReason("订单积分抵扣");
        transactionRepository.save(tx);

        log.info("积分抵扣成功: userId={}, points={}, discount={}, orderNo={}",
                userId, points, discount, orderNo);
        return Result.success(discount);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> exchangeCoupon(Long userId, Long couponId) {
        PointsCoupon coupon = couponRepository.findById(couponId).orElse(null);
        if (coupon == null || coupon.getStatus() != 1) {
            return Result.error(400, "优惠券不存在或已停用");
        }
        if (coupon.getRemainingStock() != -1 && coupon.getRemainingStock() <= 0) {
            return Result.error(400, "优惠券已兑完");
        }

        UserMembership membership = membershipRepository.findByUserIdForUpdate(userId).orElse(null);
        if (membership == null) {
            return Result.error(400, "会员信息不存在");
        }
        if (membership.getTotalPoints() < coupon.getPointsCost()) {
            return Result.error(400, "积分余额不足");
        }

        membership.setTotalPoints(membership.getTotalPoints() - coupon.getPointsCost());
        membershipRepository.save(membership);

        if (coupon.getRemainingStock() != -1) {
            coupon.setRemainingStock(coupon.getRemainingStock() - 1);
            couponRepository.save(coupon);
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setCouponName(coupon.getName());
        userCoupon.setCouponType(coupon.getCouponType());
        userCoupon.setCouponValue(coupon.getCouponValue());
        userCoupon.setMinOrderAmount(coupon.getMinOrderAmount());
        userCoupon.setStatus("UNUSED");
        userCoupon.setExpireTime(java.time.LocalDateTime.now().plusDays(coupon.getValidDays()));
        userCouponRepository.save(userCoupon);

        PointsTransaction tx = new PointsTransaction();
        tx.setUserId(userId);
        tx.setType("SPEND");
        tx.setPoints(-coupon.getPointsCost());
        tx.setBalanceAfter(membership.getTotalPoints());
        tx.setReason("兑换优惠券: " + coupon.getName());
        transactionRepository.save(tx);

        log.info("优惠券兑换成功: userId={}, couponId={}, cost={}", userId, couponId, coupon.getPointsCost());
        return Result.success();
    }

    public Result<Integer> getBalance(Long userId) {
        UserMembership membership = membershipRepository.findByUserId(userId).orElse(null);
        if (membership == null) {
            return Result.success(0);
        }
        return Result.success(membership.getTotalPoints());
    }

    public Result<Page<PointsTransaction>> getTransactions(Long userId, int page, int size) {
        Page<PointsTransaction> transactions = transactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return Result.success(transactions);
    }

    public Result<Page<PointsCoupon>> getAvailableCoupons(int page, int size) {
        Page<PointsCoupon> coupons = couponRepository
                .findByStatusOrderByPointsCostAsc(1, PageRequest.of(page, size));
        return Result.success(coupons);
    }

    public Result<List<UserCoupon>> getUserCoupons(Long userId, String status) {
        if (status != null && !status.isEmpty()) {
            return Result.success(userCouponRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status));
        }
        return Result.success(userCouponRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<BigDecimal> useCoupon(Long userId, Long userCouponId, String orderNo, BigDecimal orderAmount) {
        UserCoupon userCoupon = userCouponRepository.findByIdAndUserId(userCouponId, userId).orElse(null);
        if (userCoupon == null) {
            return Result.error(400, "优惠券不存在");
        }
        if (!"UNUSED".equals(userCoupon.getStatus())) {
            return Result.error(400, "优惠券已使用或已过期");
        }
        if (userCoupon.getExpireTime().isBefore(java.time.LocalDateTime.now())) {
            userCoupon.setStatus("EXPIRED");
            userCouponRepository.save(userCoupon);
            return Result.error(400, "优惠券已过期");
        }
        if (orderAmount.compareTo(userCoupon.getMinOrderAmount()) < 0) {
            return Result.error(400, "订单金额未达到优惠券使用门槛");
        }

        BigDecimal discount;
        if ("FIXED_AMOUNT".equals(userCoupon.getCouponType())) {
            discount = userCoupon.getCouponValue();
        } else {
            discount = orderAmount.multiply(BigDecimal.ONE.subtract(userCoupon.getCouponValue()))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        userCoupon.setStatus("USED");
        userCoupon.setUsedOrderNo(orderNo);
        userCouponRepository.save(userCoupon);

        return Result.success(discount);
    }

    // ==================== 管理员接口 ====================

    public Result<List<CategoryPointsRule>> getAllPointsRules() {
        return Result.success(pointsRuleRepository.findAll());
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<CategoryPointsRule> savePointsRule(CategoryPointsRuleRequest request) {
        CategoryPointsRule rule = pointsRuleRepository.findByCategoryId(request.getCategoryId()).orElse(null);
        if (rule == null) {
            rule = new CategoryPointsRule();
            rule.setCategoryId(request.getCategoryId());
        }
        rule.setPointsRate(request.getPointsRate());
        pointsRuleRepository.save(rule);
        return Result.success(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deletePointsRule(Long id) {
        pointsRuleRepository.deleteById(id);
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<PointsCoupon> createCoupon(PointsCouponRequest request) {
        PointsCoupon coupon = new PointsCoupon();
        coupon.setName(request.getName());
        coupon.setPointsCost(request.getPointsCost());
        coupon.setCouponType(request.getCouponType());
        coupon.setCouponValue(request.getCouponValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO);
        coupon.setValidDays(request.getValidDays() != null ? request.getValidDays() : 30);
        coupon.setTotalStock(request.getTotalStock() != null ? request.getTotalStock() : -1);
        coupon.setRemainingStock(request.getTotalStock() != null ? request.getTotalStock() : -1);
        coupon.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        couponRepository.save(coupon);
        return Result.success(coupon);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<PointsCoupon> updateCoupon(Long id, PointsCouponRequest request) {
        PointsCoupon coupon = couponRepository.findById(id).orElse(null);
        if (coupon == null) {
            return Result.error(404, "积分优惠券不存在");
        }
        if (request.getName() != null) coupon.setName(request.getName());
        if (request.getPointsCost() != null) coupon.setPointsCost(request.getPointsCost());
        if (request.getCouponType() != null) coupon.setCouponType(request.getCouponType());
        if (request.getCouponValue() != null) coupon.setCouponValue(request.getCouponValue());
        if (request.getMinOrderAmount() != null) coupon.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getValidDays() != null) coupon.setValidDays(request.getValidDays());
        if (request.getTotalStock() != null) coupon.setTotalStock(request.getTotalStock());
        if (request.getStatus() != null) coupon.setStatus(request.getStatus());
        couponRepository.save(coupon);
        return Result.success(coupon);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteCoupon(Long id) {
        couponRepository.deleteById(id);
        return Result.success();
    }

    public Result<Page<PointsCoupon>> getAllCouponsAdmin(int page, int size) {
        return Result.success(couponRepository.findAll(PageRequest.of(page, size)));
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredCoupons() {
        List<UserCoupon> expired = userCouponRepository
                .findByStatusAndExpireTimeBefore("UNUSED", java.time.LocalDateTime.now());
        for (UserCoupon coupon : expired) {
            coupon.setStatus("EXPIRED");
            userCouponRepository.save(coupon);
        }
        if (!expired.isEmpty()) {
            log.info("已清理过期优惠券: {}张", expired.size());
        }
    }
}
