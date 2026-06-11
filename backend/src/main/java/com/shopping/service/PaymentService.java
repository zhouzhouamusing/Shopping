package com.shopping.service;

import com.shopping.dto.PaymentRequest;
import com.shopping.dto.PaymentResponse;
import com.shopping.dto.Result;
import com.shopping.entity.Order;
import com.shopping.entity.OrderItem;
import com.shopping.entity.Payment;
import com.shopping.entity.Product;
import com.shopping.repository.OrderItemRepository;
import com.shopping.repository.OrderRepository;
import com.shopping.repository.PaymentRepository;
import com.shopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PointsService pointsService;

    @Value("${payment.timeout-minutes:30}")
    private int timeoutMinutes;

    // 模拟支付渠道处理状态 (生产环境由第三方回调替代)
    private final ConcurrentHashMap<String, String> channelProcessingMap = new ConcurrentHashMap<>();

    @Transactional(rollbackFor = Exception.class)
    public Result<PaymentResponse> createPayment(Long userId, PaymentRequest request) {
        Order order = orderRepository.findByOrderNo(request.getOrderNo()).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }
        if (order.getStatus() != 0) {
            return Result.error(400, "订单状态异常，无法支付");
        }

        Payment existingPayment = paymentRepository.findByOrderNoAndPaymentStatus(
                request.getOrderNo(), "PENDING").orElse(null);
        if (existingPayment != null) {
            if (existingPayment.getExpireTime().isBefore(LocalDateTime.now())) {
                existingPayment.setPaymentStatus("EXPIRED");
                paymentRepository.save(existingPayment);
            } else {
                return Result.success(buildResponse(existingPayment));
            }
        }

        String paymentNo = generatePaymentNo();
        String idempotencyKey = UUID.randomUUID().toString();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(timeoutMinutes);

        Payment payment = new Payment();
        payment.setPaymentNo(paymentNo);
        payment.setOrderNo(request.getOrderNo());
        payment.setUserId(userId);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentAmount(order.getTotalAmount());
        payment.setPaymentStatus("PENDING");
        payment.setIdempotencyKey(idempotencyKey);
        payment.setExpireTime(expireTime);

        // 货到付款：直接完成支付
        if ("COD".equals(request.getPaymentMethod())) {
            payment.setPaymentStatus("SUCCESS");
            payment.setPayTime(LocalDateTime.now());
            paymentRepository.save(payment);

            order.setStatus(1);
            order.setPaymentTime(LocalDateTime.now());
            order.setPaymentMethod("COD");
            order.setPaymentNo(paymentNo);
            orderRepository.save(order);

            log.info("货到付款订单直接完成: orderNo={}, paymentNo={}", order.getOrderNo(), paymentNo);
            pointsService.awardPointsForOrder(order.getOrderNo());
            return Result.success(buildResponse(payment));
        }

        paymentRepository.save(payment);

        // 调用模拟支付渠道
        invokePaymentChannel(payment);

        return Result.success(buildResponse(payment));
    }

    public Result<PaymentResponse> getPaymentStatus(Long userId, String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo).orElse(null);
        if (payment == null) {
            return Result.error(404, "支付记录不存在");
        }
        if (!payment.getUserId().equals(userId)) {
            return Result.error(403, "无权查看此支付记录");
        }
        return Result.success(buildResponse(payment));
    }

    public Result<PaymentResponse> getPaymentByOrderNo(Long userId, String orderNo) {
        Payment payment = paymentRepository.findByOrderNo(orderNo).orElse(null);
        if (payment == null) {
            return Result.error(404, "支付记录不存在");
        }
        if (!payment.getUserId().equals(userId)) {
            return Result.error(403, "无权查看此支付记录");
        }
        return Result.success(buildResponse(payment));
    }

    /**
     * 模拟用户确认支付 - 触发支付渠道回调
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<PaymentResponse> processPayment(Long userId, String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo).orElse(null);
        if (payment == null) {
            return Result.error(404, "支付记录不存在");
        }
        if (!payment.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此支付记录");
        }

        // 幂等性校验：已成功直接返回
        if ("SUCCESS".equals(payment.getPaymentStatus())) {
            return Result.success(buildResponse(payment));
        }

        if ("EXPIRED".equals(payment.getPaymentStatus())) {
            return Result.error(400, "支付已超时，请重新下单");
        }

        if (!"PENDING".equals(payment.getPaymentStatus())) {
            return Result.error(400, "支付状态异常");
        }

        if (payment.getExpireTime().isBefore(LocalDateTime.now())) {
            payment.setPaymentStatus("EXPIRED");
            paymentRepository.save(payment);
            return Result.error(400, "支付已超时，请重新下单");
        }

        // 模拟向支付渠道确认支付
        log.info("向支付渠道发起支付确认: paymentNo={}, method={}, amount={}",
                paymentNo, payment.getPaymentMethod(), payment.getPaymentAmount());

        // 模拟渠道返回成功，触发回调逻辑
        return handlePaymentCallback(paymentNo, "SUCCESS", generateTradeNo());
    }

    /**
     * 支付渠道异步回调处理
     * 在生产环境中，此方法由第三方支付平台的异步通知调用
     */
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500, multiplier = 2)
    )
    @Transactional(rollbackFor = Exception.class)
    public Result<PaymentResponse> handlePaymentCallback(String paymentNo, String result, String tradeNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo).orElse(null);
        if (payment == null) {
            log.error("回调处理失败，支付记录不存在: paymentNo={}", paymentNo);
            return Result.error(404, "支付记录不存在");
        }

        // 幂等性：重复回调不重复处理
        if ("SUCCESS".equals(payment.getPaymentStatus())) {
            log.info("重复回调忽略: paymentNo={}", paymentNo);
            return Result.success(buildResponse(payment));
        }

        if (!"PENDING".equals(payment.getPaymentStatus())) {
            log.warn("回调时支付状态异常: paymentNo={}, status={}", paymentNo, payment.getPaymentStatus());
            return Result.error(400, "支付状态异常");
        }

        if ("SUCCESS".equals(result)) {
            payment.setPaymentStatus("SUCCESS");
            payment.setPayTime(LocalDateTime.now());
            paymentRepository.save(payment);

            // 更新订单状态
            Order order = orderRepository.findByOrderNo(payment.getOrderNo()).orElse(null);
            if (order != null && order.getStatus() == 0) {
                order.setStatus(1);
                order.setPaymentTime(LocalDateTime.now());
                order.setPaymentMethod(payment.getPaymentMethod());
                order.setPaymentNo(paymentNo);
                orderRepository.save(order);
            }

            log.info("支付成功回调处理完成: paymentNo={}, orderNo={}, tradeNo={}",
                    paymentNo, payment.getOrderNo(), tradeNo);
            channelProcessingMap.remove(paymentNo);

            pointsService.awardPointsForOrder(payment.getOrderNo());
        } else {
            payment.setPaymentStatus("FAILED");
            paymentRepository.save(payment);
            log.info("支付失败回调: paymentNo={}, reason={}", paymentNo, result);
            channelProcessingMap.remove(paymentNo);
        }

        return Result.success(buildResponse(payment));
    }

    /**
     * 模拟外部支付渠道回调（异步通知）
     * 生产环境中由支付宝/微信等发起 HTTP POST 回调
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> receiveChannelCallback(String paymentNo, String status, String tradeNo, String sign) {
        // 模拟验签（生产环境需要用平台公钥验证签名）
        if (sign == null || sign.isEmpty()) {
            log.warn("回调验签失败: paymentNo={}", paymentNo);
            return Result.error(400, "验签失败");
        }

        log.info("收到支付渠道回调通知: paymentNo={}, status={}, tradeNo={}", paymentNo, status, tradeNo);

        String callbackResult = "SUCCESS".equals(status) ? "SUCCESS" : "FAILED";
        handlePaymentCallback(paymentNo, callbackResult, tradeNo);

        return Result.success("OK");
    }

    /**
     * 查询支付渠道状态（主动查询模式，用于回调丢失场景）
     */
    public Result<PaymentResponse> queryChannelStatus(Long userId, String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo).orElse(null);
        if (payment == null) {
            return Result.error(404, "支付记录不存在");
        }
        if (!payment.getUserId().equals(userId)) {
            return Result.error(403, "无权查看");
        }

        // 模拟主动查询渠道状态
        String channelStatus = channelProcessingMap.get(paymentNo);
        if (channelStatus != null && "PROCESSING".equals(channelStatus) && "PENDING".equals(payment.getPaymentStatus())) {
            log.info("主动查询渠道状态: paymentNo={}, 渠道处理中", paymentNo);
        }

        return Result.success(buildResponse(payment));
    }

    @Scheduled(fixedRate = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void checkPaymentTimeout() {
        List<Payment> expiredPayments = paymentRepository
                .findByPaymentStatusAndExpireTimeBefore("PENDING", LocalDateTime.now());

        for (Payment payment : expiredPayments) {
            payment.setPaymentStatus("EXPIRED");
            paymentRepository.save(payment);

            Order order = orderRepository.findByOrderNo(payment.getOrderNo()).orElse(null);
            if (order != null && order.getStatus() == 0) {
                order.setStatus(7);
                orderRepository.save(order);
                restoreStock(order.getId());
            }
            channelProcessingMap.remove(payment.getPaymentNo());
            log.info("支付超时，订单已过期: orderNo={}, paymentNo={}",
                    payment.getOrderNo(), payment.getPaymentNo());
        }
    }

    /**
     * 模拟调用支付渠道（在线支付）
     */
    private void invokePaymentChannel(Payment payment) {
        String method = payment.getPaymentMethod();
        channelProcessingMap.put(payment.getPaymentNo(), "PROCESSING");

        log.info("发起支付渠道调用: paymentNo={}, method={}, amount={}",
                payment.getPaymentNo(), method, payment.getPaymentAmount());

        switch (method) {
            case "ALIPAY":
                log.info("调用支付宝接口 - alipay.trade.precreate | 生成预支付订单");
                break;
            case "WECHAT":
                log.info("调用微信支付接口 - /pay/unifiedorder | 统一下单NATIVE模式");
                break;
            case "BANK_CARD":
                log.info("调用银行卡快捷支付接口 - 发送短信验证");
                break;
            default:
                log.info("未知支付渠道: {}", method);
        }
    }

    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(Math.max(0, product.getSales() - item.getQuantity()));
                productRepository.save(product);
            }
        }
    }

    private PaymentResponse buildResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentNo(payment.getPaymentNo());
        response.setOrderNo(payment.getOrderNo());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentAmount(payment.getPaymentAmount());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setExpireTime(payment.getExpireTime());
        response.setPayTime(payment.getPayTime());

        if ("PENDING".equals(payment.getPaymentStatus())) {
            long remaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), payment.getExpireTime());
            response.setRemainingSeconds(Math.max(0, remaining));
        } else {
            response.setRemainingSeconds(0L);
        }

        return response;
    }

    private String generatePaymentNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "PAY" + timestamp + uuid;
    }

    private String generateTradeNo() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
