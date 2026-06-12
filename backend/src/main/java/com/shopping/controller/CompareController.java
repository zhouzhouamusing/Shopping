package com.shopping.controller;

import com.shopping.dto.Result;
import com.shopping.entity.CompareItem;
import com.shopping.service.CompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compare")
@RequiredArgsConstructor
public class CompareController {

    private final CompareService compareService;

    @GetMapping
    public Result<List<CompareItem>> getCompareList(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return compareService.getCompareList(userId);
    }

    @PostMapping
    public Result<Void> addToCompare(Authentication authentication,
                                     @RequestParam Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        return compareService.addToCompare(userId, productId);
    }

    @DeleteMapping("/{productId}")
    public Result<Void> removeFromCompare(Authentication authentication,
                                          @PathVariable Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        return compareService.removeFromCompare(userId, productId);
    }

    @DeleteMapping
    public Result<Void> clearCompare(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return compareService.clearCompare(userId);
    }
}
