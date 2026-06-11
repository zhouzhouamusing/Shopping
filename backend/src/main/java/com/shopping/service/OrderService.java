package com.shopping.service;

import com.shopping.dto.OrderRequest;
import com.shopping.dto.Result;
import com.shopping.entity.*;
import com.shopping.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 订单状态流转：
 * 0-待付款 → 1-待发货(已付款) → 2-待收货(已发货) → 3-待评价(已签收) → 4-已完成
 * 0-待付款 → 5-已取消(用户取消)
 * 0-待付款 → 7-已过期(支付超时)
 * 1-待发货 → 6-已退款
 * 2-待收货 → 6-已退款
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PriceCircuitBreakerService priceCircuitBreaker;
    private final OrderEventQueueService eventQueue;
    private final PointsService pointsService;
    private final MembershipService membershipService;

    @Retryable(
        retryFor = {PessimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2)
    )
    @Transactional(rollbackFor = Exception.class)
    public Result<Order> createOrder(Long userId, OrderRequest request) {
        // 如果指定了地址ID，从保存的地址加载收货信息
        if (request.getAddressId() != null) {
            Address address = addressRepository.findByIdAndUserId(request.getAddressId(), userId).orElse(null);
            if (address == null) {
                return Result.error(400, "收货地址不存在");
            }
            request.setReceiverName(address.getReceiverName());
            request.setReceiverPhone(address.getPhone());
            String fullAddress = (address.getProvince() != null ? address.getProvince() : "")
                    + (address.getCity() != null ? address.getCity() : "")
                    + (address.getDistrict() != null ? address.getDistrict() : "")
                    + address.getDetailAddress();
            request.setReceiverAddress(fullAddress);
        }

        if (request.getReceiverName() == null || request.getReceiverName().isBlank()) {
            return Result.error(400, "收货人姓名不能为空");
        }
        if (request.getReceiverPhone() == null || request.getReceiverPhone().isBlank()) {
            return Result.error(400, "收货人电话不能为空");
        }
        if (request.getReceiverAddress() == null || request.getReceiverAddress().isBlank()) {
            return Result.error(400, "收货地址不能为空");
        }

        List<CartItem> cartItems = cartItemRepository.findByUserIdAndSelected(userId, 1);
        if (cartItems.isEmpty()) {
            return Result.error(400, "购物车中没有选中的商品");
        }

        String orderNo = generateOrderNo();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findByIdForUpdate(cartItem.getProductId()).orElse(null);
            if (product == null || product.getStatus() != 1) {
                return Result.error(400, "商品 " + (product != null ? product.getName() : "未知") + " 已下架");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                return Result.error(400, "商品 " + product.getName() + " 库存不足");
            }

            // 价格熔断校验
            PriceCircuitBreakerService.PriceValidationResult priceCheck =
                    priceCircuitBreaker.validatePrice(product.getPrice(), "下单商品:" + product.getName());
            if (!priceCheck.isValid()) {
                log.error("价格熔断拦截下单: product={}, price={}, reason={}",
                        product.getName(), product.getPrice(), priceCheck.getMessage());
                return Result.error(400, priceCheck.getMessage());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            product.setSales(product.getSales() + cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);

            totalAmount = totalAmount.add(itemTotal);
        }

        // 订单总金额熔断校验
        PriceCircuitBreakerService.PriceValidationResult amountCheck =
                priceCircuitBreaker.validateOrderAmount(totalAmount, orderNo);
        if (!amountCheck.isValid()) {
            log.error("订单金额熔断拦截: orderNo={}, amount={}, reason={}",
                    orderNo, totalAmount, amountCheck.getMessage());
            return Result.error(400, amountCheck.getMessage());
        }

        // 会员折扣
        BigDecimal actualAmount = totalAmount;
        Result<BigDecimal> discountResult = membershipService.getDiscountRate(userId);
        if (discountResult.getCode() == 200 && discountResult.getData() != null) {
            BigDecimal discountRate = discountResult.getData();
            if (discountRate.compareTo(BigDecimal.ONE) < 0) {
                actualAmount = totalAmount.multiply(discountRate).setScale(2, java.math.RoundingMode.HALF_UP);
            }
        }

        // 积分抵扣
        int pointsUsed = 0;
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        if (request.getUsePoints() != null && request.getUsePoints() > 0) {
            Result<BigDecimal> pointsResult = pointsService.redeemPointsForOrder(userId, request.getUsePoints(), orderNo);
            if (pointsResult.getCode() == 200 && pointsResult.getData() != null) {
                pointsDiscount = pointsResult.getData();
                pointsUsed = request.getUsePoints();
                actualAmount = actualAmount.subtract(pointsDiscount).max(new BigDecimal("0.01"));
            }
        }

        // 优惠券抵扣
        Long couponId = null;
        BigDecimal couponDiscount = BigDecimal.ZERO;
        if (request.getUserCouponId() != null) {
            Result<BigDecimal> couponResult = pointsService.useCoupon(userId, request.getUserCouponId(), orderNo, actualAmount);
            if (couponResult.getCode() == 200 && couponResult.getData() != null) {
                couponDiscount = couponResult.getData();
                couponId = request.getUserCouponId();
                actualAmount = actualAmount.subtract(couponDiscount).max(new BigDecimal("0.01"));
            }
        }

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setActualAmount(actualAmount);
        order.setPointsUsed(pointsUsed);
        order.setPointsDiscount(pointsDiscount);
        order.setCouponId(couponId);
        order.setCouponDiscount(couponDiscount);
        order.setStatus(0);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setRemark(request.getRemark());

        orderRepository.save(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
        }
        orderItemRepository.saveAll(orderItems);

        for (CartItem cartItem : cartItems) {
            cartItemRepository.delete(cartItem);
        }

        order.setOrderItems(orderItems);
        return Result.success(order);
    }

    public Result<Page<Order>> getUserOrders(Long userId, int page, int size) {
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return Result.success(orders);
    }

    public Result<Order> getOrderDetail(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权查看此订单");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        order.setOrderItems(items);
        return Result.success(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelOrder(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }
        if (order.getStatus() != 0) {
            return Result.error(400, "只能取消待付款订单");
        }

        order.setStatus(5);
        orderRepository.save(order);
        restoreStock(order.getId());
        eventQueue.publishStatusChangeEvent(orderNo, 0, 5, "user:" + userId, "用户主动取消");
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> payOrder(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }
        if (order.getStatus() != 0) {
            return Result.error(400, "订单状态异常");
        }

        order.setStatus(1);
        order.setPaymentTime(LocalDateTime.now());
        orderRepository.save(order);
        eventQueue.publishStatusChangeEvent(orderNo, 0, 1, "user:" + userId, "支付完成");
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> confirmReceive(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }
        if (order.getStatus() != 2) {
            return Result.error(400, "只能对已发货订单确认收货");
        }

        order.setStatus(3);
        order.setFinishTime(LocalDateTime.now());
        orderRepository.save(order);
        eventQueue.publishStatusChangeEvent(orderNo, 2, 3, "user:" + userId, "确认收货");
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> completeOrder(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }
        if (order.getStatus() != 3) {
            return Result.error(400, "只能对待评价订单完成操作");
        }

        order.setStatus(4);
        orderRepository.save(order);
        eventQueue.publishStatusChangeEvent(orderNo, 3, 4, "user:" + userId, "完成评价");
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> refundOrder(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }
        int fromStatus = order.getStatus();
        if (fromStatus != 1 && fromStatus != 2) {
            return Result.error(400, "当前状态不可申请退款");
        }

        order.setStatus(6);
        orderRepository.save(order);
        restoreStock(order.getId());
        eventQueue.publishStatusChangeEvent(orderNo, fromStatus, 6, "user:" + userId, "用户申请退款");
        pointsService.deductPointsForRefund(orderNo);
        return Result.success();
    }

    public Result<Page<Order>> getAllOrders(int page, int size, Integer status) {
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size));
        } else {
            orders = orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        }
        return Result.success(orders);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deliverOrder(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (order.getStatus() != 1) {
            return Result.error(400, "只能对已付款订单发货");
        }

        order.setStatus(2);
        order.setDeliveryTime(LocalDateTime.now());
        orderRepository.save(order);
        eventQueue.publishStatusChangeEvent(orderNo, 1, 2, "admin", "管理员发货");
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> adminRefundOrder(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        int fromStatus = order.getStatus();
        if (fromStatus != 1 && fromStatus != 2) {
            return Result.error(400, "当前状态不可退款");
        }

        order.setStatus(6);
        orderRepository.save(order);
        restoreStock(order.getId());
        eventQueue.publishStatusChangeEvent(orderNo, fromStatus, 6, "admin", "管理员退款");
        pointsService.deductPointsForRefund(orderNo);
        log.info("管理员退款: orderNo={}", orderNo);
        return Result.success();
    }

    public void restoreStock(Long orderId) {
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

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD" + timestamp + uuid;
    }
}
