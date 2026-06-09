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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Value("${payment.timeout-minutes:30}")
    private int timeoutMinutes;

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

        if ("COD".equals(request.getPaymentMethod())) {
            payment.setPaymentStatus("SUCCESS");
            payment.setPayTime(LocalDateTime.now());
            paymentRepository.save(payment);

            order.setStatus(1);
            order.setPaymentTime(LocalDateTime.now());
            order.setPaymentMethod("COD");
            order.setPaymentNo(paymentNo);
            orderRepository.save(order);

            return Result.success(buildResponse(payment));
        }

        paymentRepository.save(payment);
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

    @Transactional(rollbackFor = Exception.class)
    public Result<PaymentResponse> processPayment(Long userId, String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo).orElse(null);
        if (payment == null) {
            return Result.error(404, "支付记录不存在");
        }
        if (!payment.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此支付记录");
        }

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

        payment.setPaymentStatus("SUCCESS");
        payment.setPayTime(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = orderRepository.findByOrderNo(payment.getOrderNo()).orElse(null);
        if (order != null && order.getStatus() == 0) {
            order.setStatus(1);
            order.setPaymentTime(LocalDateTime.now());
            order.setPaymentMethod(payment.getPaymentMethod());
            order.setPaymentNo(paymentNo);
            orderRepository.save(order);
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
                order.setStatus(4);
                orderRepository.save(order);
                restoreStock(order.getId());
            }
            log.info("支付超时，订单已取消: orderNo={}, paymentNo={}",
                    payment.getOrderNo(), payment.getPaymentNo());
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
}
