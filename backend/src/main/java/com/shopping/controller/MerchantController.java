package com.shopping.controller;

import com.shopping.dto.ProductRequest;
import com.shopping.dto.Result;
import com.shopping.dto.ReviewReplyRequest;
import com.shopping.entity.Order;
import com.shopping.entity.Product;
import com.shopping.entity.Review;
import com.shopping.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(Authentication authentication) {
        Long merchantId = (Long) authentication.getPrincipal();
        return merchantService.getDashboard(merchantId);
    }

    @GetMapping("/products")
    public Result<Page<Product>> getMyProducts(Authentication authentication,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Long merchantId = (Long) authentication.getPrincipal();
        return merchantService.getMyProducts(merchantId, page, size);
    }

    @PostMapping("/products")
    public Result<Product> addProduct(Authentication authentication,
                                      @Valid @RequestBody ProductRequest request) {
        Long merchantId = (Long) authentication.getPrincipal();
        return merchantService.addProduct(merchantId, request);
    }

    @PutMapping("/products/{id}")
    public Result<Product> updateProduct(Authentication authentication,
                                          @PathVariable Long id,
                                          @Valid @RequestBody ProductRequest request) {
        Long merchantId = (Long) authentication.getPrincipal();
        return merchantService.updateProduct(merchantId, id, request);
    }

    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(Authentication authentication,
                                       @PathVariable Long id) {
        Long merchantId = (Long) authentication.getPrincipal();
        return merchantService.deleteProduct(merchantId, id);
    }

    @GetMapping("/orders")
    public Result<Page<Order>> getMyOrders(Authentication authentication,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Integer status) {
        Long merchantId = (Long) authentication.getPrincipal();
        return merchantService.getMyOrders(merchantId, page, size, status);
    }

    @PutMapping("/orders/{orderNo}/deliver")
    public Result<Void> deliverOrder(Authentication authentication,
                                      @PathVariable String orderNo) {
        Long merchantId = (Long) authentication.getPrincipal();
        return merchantService.verifyAndDeliverOrder(merchantId, orderNo);
    }

    @GetMapping("/reviews")
    public Result<Page<Review>> getMyReviews(Authentication authentication,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        Long merchantId = (Long) authentication.getPrincipal();
        return merchantService.getMyReviews(merchantId, page, size);
    }

    @PutMapping("/reviews/{id}/reply")
    public Result<Void> replyReview(Authentication authentication,
                                     @PathVariable Long id,
                                     @RequestBody ReviewReplyRequest request) {
        Long merchantId = (Long) authentication.getPrincipal();
        return merchantService.replyReview(merchantId, id, request.getAdminReply());
    }
}
