package com.shopping.controller;

import com.shopping.dto.PaymentRequest;
import com.shopping.dto.PaymentResponse;
import com.shopping.dto.Result;
import com.shopping.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public Result<PaymentResponse> createPayment(Authentication authentication,
                                                  @Valid @RequestBody PaymentRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        return paymentService.createPayment(userId, request);
    }

    @GetMapping("/{paymentNo}")
    public Result<PaymentResponse> getPaymentStatus(Authentication authentication,
                                                     @PathVariable String paymentNo) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        return paymentService.getPaymentStatus(userId, paymentNo);
    }

    @PutMapping("/{paymentNo}/pay")
    public Result<PaymentResponse> processPayment(Authentication authentication,
                                                   @PathVariable String paymentNo) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        return paymentService.processPayment(userId, paymentNo);
    }

    @GetMapping("/order/{orderNo}")
    public Result<PaymentResponse> getPaymentByOrderNo(Authentication authentication,
                                                        @PathVariable String orderNo) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        return paymentService.getPaymentByOrderNo(userId, orderNo);
    }
}
