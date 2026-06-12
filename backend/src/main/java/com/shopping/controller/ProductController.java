package com.shopping.controller;

import com.shopping.dto.Result;
import com.shopping.entity.Product;
import com.shopping.entity.Review;
import com.shopping.service.ProductService;
import com.shopping.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @GetMapping
    public Result<Page<Product>> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        return productService.getProductList(page, size, categoryId, keyword, sort);
    }

    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    @GetMapping("/hot")
    public Result<Page<Product>> getHotProducts(@RequestParam(defaultValue = "8") int size) {
        return productService.getHotProducts(size);
    }

    @GetMapping("/new")
    public Result<Page<Product>> getNewProducts(@RequestParam(defaultValue = "8") int size) {
        return productService.getNewProducts(size);
    }

    @GetMapping("/{id}/reviews")
    public Result<Page<Review>> getProductReviews(@PathVariable Long id,
                                                   @RequestParam(required = false) Integer rating,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "newest") String sort) {
        return reviewService.getProductReviews(id, rating, page, size, sort);
    }

    @GetMapping("/batch")
    public Result<List<Product>> getProductsByIds(@RequestParam List<Long> ids) {
        return productService.getProductsByIds(ids);
    }
}
