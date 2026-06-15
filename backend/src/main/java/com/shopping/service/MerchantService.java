package com.shopping.service;

import com.shopping.dto.ProductRequest;
import com.shopping.dto.Result;
import com.shopping.entity.Order;
import com.shopping.entity.OrderItem;
import com.shopping.entity.Product;
import com.shopping.entity.Review;
import com.shopping.repository.OrderItemRepository;
import com.shopping.repository.OrderRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;

    public Result<Map<String, Object>> getDashboard(Long merchantId) {
        long totalProducts = productRepository.countByMerchantId(merchantId);
        long totalOrders = orderRepository.countByMerchantId(merchantId);
        long pendingOrders = orderRepository.countByMerchantIdAndStatus(merchantId, 1);

        Map<String, Object> data = new HashMap<>();
        data.put("totalProducts", totalProducts);
        data.put("totalOrders", totalOrders);
        data.put("pendingOrders", pendingOrders);
        return Result.success(data);
    }

    public Result<Page<Product>> getMyProducts(Long merchantId, int page, int size) {
        Page<Product> products = productRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId, PageRequest.of(page, size));
        return Result.success(products);
    }

    public Result<Product> addProduct(Long merchantId, ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStock(request.getStock());
        product.setMainImage(request.getMainImage());
        product.setImages(request.getImages());
        product.setCategoryId(request.getCategoryId());
        product.setStatus(request.getStatus());
        product.setMerchantId(merchantId);

        productRepository.save(product);
        return Result.success(product);
    }

    public Result<Product> updateProduct(Long merchantId, Long productId, ProductRequest request) {
        Product product = productRepository.findByIdAndMerchantId(productId, merchantId).orElse(null);
        if (product == null) {
            return Result.error(404, "商品不存在或无权操作");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStock(request.getStock());
        product.setMainImage(request.getMainImage());
        product.setImages(request.getImages());
        product.setCategoryId(request.getCategoryId());
        product.setStatus(request.getStatus());

        productRepository.save(product);
        return Result.success(product);
    }

    public Result<Void> deleteProduct(Long merchantId, Long productId) {
        Product product = productRepository.findByIdAndMerchantId(productId, merchantId).orElse(null);
        if (product == null) {
            return Result.error(404, "商品不存在或无权操作");
        }
        productRepository.delete(product);
        return Result.success();
    }

    public Result<Page<Order>> getMyOrders(Long merchantId, int page, int size, Integer status) {
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByMerchantIdAndStatus(merchantId, status, PageRequest.of(page, size));
        } else {
            orders = orderRepository.findByMerchantId(merchantId, PageRequest.of(page, size));
        }
        return Result.success(orders);
    }

    public Result<Page<Review>> getMyReviews(Long merchantId, int page, int size) {
        Page<Review> reviews = reviewRepository.findByMerchantId(merchantId, PageRequest.of(page, size));
        return Result.success(reviews);
    }

    public Result<Void> replyReview(Long merchantId, Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return Result.error(404, "评价不存在");
        }
        Product product = productRepository.findById(review.getProductId()).orElse(null);
        if (product == null || !merchantId.equals(product.getMerchantId())) {
            return Result.error(403, "无权操作此评价");
        }
        review.setAdminReply(reply);
        reviewRepository.save(review);
        return Result.success();
    }

    public Result<Void> verifyAndDeliverOrder(Long merchantId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (order.getStatus() != 1) {
            return Result.error(400, "只能对已付款订单发货");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        boolean hasOwnProduct = false;
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null && merchantId.equals(product.getMerchantId())) {
                hasOwnProduct = true;
                break;
            }
        }
        if (!hasOwnProduct) {
            return Result.error(403, "该订单不包含您的商品，无权发货");
        }

        order.setStatus(2);
        order.setDeliveryTime(java.time.LocalDateTime.now());
        orderRepository.save(order);
        return Result.success();
    }
}
