package com.shopping.controller;

import com.shopping.dto.Result;
import com.shopping.entity.BrowsingHistory;
import com.shopping.service.BrowsingHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class BrowsingHistoryController {

    private final BrowsingHistoryService browsingHistoryService;

    @PostMapping
    public Result<Void> recordHistory(Authentication authentication,
                                      @RequestParam Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        browsingHistoryService.recordHistory(userId, productId);
        return Result.success();
    }

    @GetMapping
    public Result<Page<BrowsingHistory>> getHistory(Authentication authentication,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return browsingHistoryService.getHistory(userId, page, size);
    }

    @DeleteMapping("/{productId}")
    public Result<Void> deleteHistory(Authentication authentication,
                                      @PathVariable Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        return browsingHistoryService.deleteHistory(userId, productId);
    }

    @DeleteMapping
    public Result<Void> clearAllHistory(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return browsingHistoryService.clearAllHistory(userId);
    }
}
