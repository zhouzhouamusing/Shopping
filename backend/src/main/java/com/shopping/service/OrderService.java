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

    @Retryable(
        retryFor = {PessimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2)
    )
    @Transactional(rollbackFor = Exception.class)
    public Result<Order> createOrder(Long userId, OrderRequest request) {
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

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
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
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            return Result.error(400, "当前状态不可申请退款");
        }

        order.setStatus(6);
        orderRepository.save(order);
        restoreStock(order.getId());
        log.info("订单退款完成: orderNo={}", orderNo);
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
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> adminRefundOrder(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            return Result.error(400, "当前状态不可退款");
        }

        order.setStatus(6);
        orderRepository.save(order);
        restoreStock(order.getId());
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
