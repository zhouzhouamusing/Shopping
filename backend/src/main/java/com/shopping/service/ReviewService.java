package com.shopping.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.dto.ReviewRequest;
import com.shopping.dto.Result;
import com.shopping.entity.*;
import com.shopping.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public Result<Review> createReview(Long userId, ReviewRequest request) {
        if (reviewRepository.existsByOrderItemId(request.getOrderItemId())) {
            return Result.error(400, "该商品已评价");
        }

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId()).orElse(null);
        if (orderItem == null) {
            return Result.error(404, "订单项不存在");
        }

        Order order = orderRepository.findById(orderItem.getOrderId()).orElse(null);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权评价此商品");
        }
        if (order.getStatus() != 3 && order.getStatus() != 4) {
            return Result.error(400, "订单状态不允许评价");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(orderItem.getProductId());
        review.setOrderItemId(request.getOrderItemId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setStatus("PENDING");

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                review.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                log.error("序列化评价图片失败", e);
            }
        }

        reviewRepository.save(review);
        updateProductRating(orderItem.getProductId());

        return Result.success(review);
    }

    public Result<Page<Review>> getProductReviews(Long productId, Integer rating, int page, int size, String sort) {
        Sort sortOrder;
        switch (sort) {
            case "oldest":
                sortOrder = Sort.by(Sort.Direction.ASC, "createdAt");
                break;
            case "rating_high":
                sortOrder = Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));
                break;
            case "rating_low":
                sortOrder = Sort.by(Sort.Direction.ASC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));
                break;
            default:
                sortOrder = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }

        PageRequest pageRequest = PageRequest.of(page, size, sortOrder);
        Page<Review> reviews;
        if (rating != null && rating >= 1 && rating <= 5) {
            reviews = reviewRepository.findByProductIdAndStatusAndRating(
                    productId, "APPROVED", rating, pageRequest);
        } else {
            reviews = reviewRepository.findByProductIdAndStatus(
                    productId, "APPROVED", pageRequest);
        }

        reviews.getContent().forEach(this::enrichReview);
        return Result.success(reviews);
    }

    public Result<Page<Review>> getUserReviews(Long userId, int page, int size) {
        Page<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        reviews.getContent().forEach(this::enrichReview);
        return Result.success(reviews);
    }

    public Result<Page<Review>> getAllReviews(int page, int size, String status) {
        Page<Review> reviews;
        if (status != null && !status.isEmpty()) {
            reviews = reviewRepository.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size));
        } else {
            reviews = reviewRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        }
        reviews.getContent().forEach(this::enrichReview);
        return Result.success(reviews);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return Result.error(404, "评价不存在");
        }
        review.setStatus("APPROVED");
        reviewRepository.save(review);
        updateProductRating(review.getProductId());
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return Result.error(404, "评价不存在");
        }
        review.setStatus("REJECTED");
        reviewRepository.save(review);
        updateProductRating(review.getProductId());
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> replyReview(Long reviewId, String adminReply) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return Result.error(404, "评价不存在");
        }
        review.setAdminReply(adminReply);
        reviewRepository.save(review);
        return Result.success();
    }

    private void updateProductRating(Long productId) {
        Double avg = reviewRepository.getAverageRatingByProductId(productId);
        Long count = reviewRepository.getApprovedCountByProductId(productId);

        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setAverageRating(avg != null ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP) : null);
            product.setReviewCount(count != null ? count.intValue() : 0);
            productRepository.save(product);
        }
    }

    private void enrichReview(Review review) {
        userRepository.findById(review.getUserId()).ifPresent(user -> {
            review.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
            review.setUserAvatar(user.getAvatar());
        });

        productRepository.findById(review.getProductId()).ifPresent(product -> {
            review.setProductName(product.getName());
        });

        if (review.getImages() != null && !review.getImages().isEmpty()) {
            try {
                List<String> imageList = objectMapper.readValue(review.getImages(), new TypeReference<List<String>>() {});
                review.setImageList(imageList);
            } catch (JsonProcessingException e) {
                review.setImageList(new ArrayList<>());
            }
        } else {
            review.setImageList(new ArrayList<>());
        }
    }
}
