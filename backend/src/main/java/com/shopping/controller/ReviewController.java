package com.shopping.controller;

import com.shopping.dto.ReviewRequest;
import com.shopping.dto.Result;
import com.shopping.entity.Review;
import com.shopping.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<Review> createReview(Authentication authentication, @Valid @RequestBody ReviewRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return reviewService.createReview(userId, request);
    }

    @GetMapping("/my")
    public Result<Page<Review>> getMyReviews(Authentication authentication,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return reviewService.getUserReviews(userId, page, size);
    }
}
