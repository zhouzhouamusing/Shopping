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

    @GetMapping("/{paymentNo}/query")
    public Result<PaymentResponse> queryChannelStatus(Authentication authentication,
                                                       @PathVariable String paymentNo) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        return paymentService.queryChannelStatus(userId, paymentNo);
    }

    /**
     * 支付渠道异步回调入口（无需认证，由第三方支付平台调用）
     * 生产环境需配置IP白名单和签名验证
     */
    @PostMapping("/callback/{paymentNo}")
    public Result<String> paymentCallback(@PathVariable String paymentNo,
                                           @RequestParam String status,
                                           @RequestParam(required = false) String tradeNo,
                                           @RequestParam(required = false) String sign) {
        return paymentService.receiveChannelCallback(paymentNo, status, tradeNo, sign);
    }
}
