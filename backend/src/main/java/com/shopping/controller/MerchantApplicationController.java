package com.shopping.controller;

import com.shopping.dto.MerchantApplicationRequest;
import com.shopping.dto.Result;
import com.shopping.entity.MerchantApplication;
import com.shopping.service.MerchantApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant-application")
@RequiredArgsConstructor
public class MerchantApplicationController {

    private final MerchantApplicationService applicationService;

    @PostMapping
    public Result<MerchantApplication> submitApplication(Authentication authentication,
                                                          @Valid @RequestBody MerchantApplicationRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return applicationService.submitApplication(userId, request);
    }

    @GetMapping("/my")
    public Result<MerchantApplication> getMyApplication(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return applicationService.getMyApplication(userId);
    }
}
