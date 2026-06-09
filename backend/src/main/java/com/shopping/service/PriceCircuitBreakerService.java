package com.shopping.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 价格熔断降级服务
 *
 * 功能：
 * 1. 检测异常价格（负数、零值、超高价）
 * 2. 熔断阈值：连续5次价格异常触发熔断
 * 3. 降级策略：熔断状态下使用缓存价格或拒绝操作
 * 4. 恢复机制：熔断30秒后进入半开状态，连续3次正常后恢复
 */
@Slf4j
@Service
public class PriceCircuitBreakerService {

    // 熔断状态
    private enum State { CLOSED, OPEN, HALF_OPEN }

    private volatile State state = State.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger halfOpenSuccessCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong totalAnomalyCount = new AtomicLong(0);
    private final AtomicLong totalCheckCount = new AtomicLong(0);

    // 熔断配置
    private static final int FAILURE_THRESHOLD = 5;
    private static final long RESET_TIMEOUT_MS = 30000;
    private static final int HALF_OPEN_SUCCESS_THRESHOLD = 3;

    // 价格边界配置
    private static final BigDecimal MIN_VALID_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_VALID_PRICE = new BigDecimal("999999.99");
    private static final BigDecimal MAX_SINGLE_ORDER_AMOUNT = new BigDecimal("500000.00");

    /**
     * 校验商品价格是否合法
     * @return 校验结果对象
     */
    public PriceValidationResult validatePrice(BigDecimal price, String context) {
        totalCheckCount.incrementAndGet();

        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime.get() >= RESET_TIMEOUT_MS) {
                transitionToHalfOpen();
            } else {
                log.warn("[价格熔断] 熔断开启中，拒绝价格校验请求: context={}", context);
                return PriceValidationResult.circuitOpen("价格服务暂时不可用，请稍后重试");
            }
        }

        PriceValidationResult result = doValidatePrice(price, context);

        if (!result.isValid()) {
            onFailure(price, context);
        } else {
            onSuccess();
        }

        return result;
    }

    /**
     * 校验订单总金额
     */
    public PriceValidationResult validateOrderAmount(BigDecimal amount, String orderNo) {
        totalCheckCount.incrementAndGet();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            onFailure(amount, "订单金额异常: " + orderNo);
            return PriceValidationResult.invalid("订单金额不能为空或负数");
        }

        if (amount.compareTo(MAX_SINGLE_ORDER_AMOUNT) > 0) {
            onFailure(amount, "订单金额超限: " + orderNo);
            return PriceValidationResult.invalid(
                    String.format("订单金额 ¥%s 超出单笔限额 ¥%s", amount, MAX_SINGLE_ORDER_AMOUNT));
        }

        onSuccess();
        return PriceValidationResult.valid();
    }

    /**
     * 批量校验商品价格（用于购物车结算）
     */
    public PriceValidationResult validateCartPrices(java.util.List<BigDecimal> prices, String userId) {
        for (int i = 0; i < prices.size(); i++) {
            PriceValidationResult result = validatePrice(prices.get(i), "用户" + userId + "购物车商品" + (i + 1));
            if (!result.isValid()) {
                return result;
            }
        }
        return PriceValidationResult.valid();
    }

    /**
     * 获取熔断器状态（用于监控）
     */
    public CircuitBreakerStatus getStatus() {
        CircuitBreakerStatus status = new CircuitBreakerStatus();
        status.state = state.name();
        status.failureCount = failureCount.get();
        status.totalAnomalyCount = totalAnomalyCount.get();
        status.totalCheckCount = totalCheckCount.get();
        status.lastFailureTime = lastFailureTime.get();
        status.halfOpenSuccessCount = halfOpenSuccessCount.get();
        return status;
    }

    /**
     * 手动重置熔断器（管理员操作）
     */
    public void reset() {
        state = State.CLOSED;
        failureCount.set(0);
        halfOpenSuccessCount.set(0);
        log.info("[价格熔断] 手动重置熔断器");
    }

    // ===== 内部方法 =====

    private PriceValidationResult doValidatePrice(BigDecimal price, String context) {
        if (price == null) {
            return PriceValidationResult.invalid("价格不能为空");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            log.error("[价格异常] 检测到负数价格: price={}, context={}", price, context);
            return PriceValidationResult.invalid("检测到异常负数价格: ¥" + price);
        }

        if (price.compareTo(MIN_VALID_PRICE) < 0) {
            log.error("[价格异常] 价格低于最小值: price={}, min={}, context={}", price, MIN_VALID_PRICE, context);
            return PriceValidationResult.invalid("价格不能低于 ¥" + MIN_VALID_PRICE);
        }

        if (price.compareTo(MAX_VALID_PRICE) > 0) {
            log.error("[价格异常] 价格超出最大值: price={}, max={}, context={}", price, MAX_VALID_PRICE, context);
            return PriceValidationResult.invalid(
                    String.format("价格 ¥%s 超出允许的最大值 ¥%s", price, MAX_VALID_PRICE));
        }

        return PriceValidationResult.valid();
    }

    private void onFailure(BigDecimal price, String context) {
        totalAnomalyCount.incrementAndGet();
        int failures = failureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());

        log.warn("[价格熔断] 价格异常 ({}/{}): price={}, context={}",
                failures, FAILURE_THRESHOLD, price, context);

        if (state == State.HALF_OPEN) {
            transitionToOpen();
            log.warn("[价格熔断] 半开状态检测到异常，重新开启熔断");
        } else if (failures >= FAILURE_THRESHOLD && state == State.CLOSED) {
            transitionToOpen();
            log.error("[价格熔断] 触发熔断！连续{}次价格异常，熔断开启", FAILURE_THRESHOLD);
        }
    }

    private void onSuccess() {
        if (state == State.HALF_OPEN) {
            int successes = halfOpenSuccessCount.incrementAndGet();
            if (successes >= HALF_OPEN_SUCCESS_THRESHOLD) {
                transitionToClosed();
                log.info("[价格熔断] 半开状态连续{}次正常，恢复关闭状态", HALF_OPEN_SUCCESS_THRESHOLD);
            }
        } else if (state == State.CLOSED) {
            // 正常时缓慢减少失败计数
            int current = failureCount.get();
            if (current > 0) {
                failureCount.decrementAndGet();
            }
        }
    }

    private void transitionToOpen() {
        state = State.OPEN;
        halfOpenSuccessCount.set(0);
    }

    private void transitionToHalfOpen() {
        state = State.HALF_OPEN;
        halfOpenSuccessCount.set(0);
        log.info("[价格熔断] 进入半开状态，开始探测");
    }

    private void transitionToClosed() {
        state = State.CLOSED;
        failureCount.set(0);
        halfOpenSuccessCount.set(0);
    }

    // ===== 内部类 =====

    public static class PriceValidationResult {
        private final boolean valid;
        private final boolean circuitOpen;
        private final String message;

        private PriceValidationResult(boolean valid, boolean circuitOpen, String message) {
            this.valid = valid;
            this.circuitOpen = circuitOpen;
            this.message = message;
        }

        public static PriceValidationResult valid() {
            return new PriceValidationResult(true, false, null);
        }

        public static PriceValidationResult invalid(String message) {
            return new PriceValidationResult(false, false, message);
        }

        public static PriceValidationResult circuitOpen(String message) {
            return new PriceValidationResult(false, true, message);
        }

        public boolean isValid() { return valid; }
        public boolean isCircuitOpen() { return circuitOpen; }
        public String getMessage() { return message; }
    }

    public static class CircuitBreakerStatus {
        public String state;
        public int failureCount;
        public long totalAnomalyCount;
        public long totalCheckCount;
        public long lastFailureTime;
        public int halfOpenSuccessCount;
    }
}
