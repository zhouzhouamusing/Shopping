package com.shopping.service;

import com.shopping.dto.Result;
import com.shopping.entity.CompareItem;
import com.shopping.repository.CompareItemRepository;
import com.shopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompareService {

    private final CompareItemRepository compareItemRepository;
    private final ProductRepository productRepository;

    private static final int MAX_COMPARE_COUNT = 4;

    public Result<List<CompareItem>> getCompareList(Long userId) {
        List<CompareItem> items = compareItemRepository.findByUserIdOrderByCreatedAtAsc(userId);
        return Result.success(items);
    }

    @Transactional
    public Result<Void> addToCompare(Long userId, Long productId) {
        if (!productRepository.existsById(productId)) {
            return Result.error(404, "商品不存在");
        }
        if (compareItemRepository.existsByUserIdAndProductId(userId, productId)) {
            return Result.error(400, "该商品已在对比列表中");
        }
        if (compareItemRepository.countByUserId(userId) >= MAX_COMPARE_COUNT) {
            return Result.error(400, "最多只能对比" + MAX_COMPARE_COUNT + "个商品");
        }

        CompareItem item = new CompareItem();
        item.setUserId(userId);
        item.setProductId(productId);
        compareItemRepository.save(item);
        return Result.success();
    }

    @Transactional
    public Result<Void> removeFromCompare(Long userId, Long productId) {
        compareItemRepository.deleteByUserIdAndProductId(userId, productId);
        return Result.success();
    }

    @Transactional
    public Result<Void> clearCompare(Long userId) {
        compareItemRepository.deleteByUserId(userId);
        return Result.success();
    }
}
