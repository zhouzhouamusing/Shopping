package com.shopping.service;

import com.shopping.dto.Result;
import com.shopping.entity.BrowsingHistory;
import com.shopping.repository.BrowsingHistoryRepository;
import com.shopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrowsingHistoryService {

    private final BrowsingHistoryRepository browsingHistoryRepository;
    private final ProductRepository productRepository;

    private static final int MAX_HISTORY_COUNT = 50;

    @Transactional
    public Result<Void> recordHistory(Long userId, Long productId) {
        if (!productRepository.existsById(productId)) {
            return Result.error(404, "商品不存在");
        }

        var existing = browsingHistoryRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            BrowsingHistory history = existing.get();
            history.setBrowsedAt(LocalDateTime.now());
            browsingHistoryRepository.save(history);
        } else {
            BrowsingHistory history = new BrowsingHistory();
            history.setUserId(userId);
            history.setProductId(productId);
            history.setBrowsedAt(LocalDateTime.now());
            browsingHistoryRepository.save(history);

            // 超过上限则删除最早记录
            long count = browsingHistoryRepository.countByUserId(userId);
            if (count > MAX_HISTORY_COUNT) {
                List<BrowsingHistory> all = browsingHistoryRepository.findByUserIdOrderByBrowsedAtAsc(userId);
                int toDelete = (int) (count - MAX_HISTORY_COUNT);
                List<BrowsingHistory> oldest = all.subList(0, toDelete);
                browsingHistoryRepository.deleteAll(oldest);
            }
        }
        return Result.success();
    }

    public Result<Page<BrowsingHistory>> getHistory(Long userId, int page, int size) {
        Page<BrowsingHistory> histories = browsingHistoryRepository.findByUserIdOrderByBrowsedAtDesc(
                userId, PageRequest.of(page, size));
        return Result.success(histories);
    }

    @Transactional
    public Result<Void> deleteHistory(Long userId, Long productId) {
        browsingHistoryRepository.deleteByUserIdAndProductId(userId, productId);
        return Result.success();
    }

    @Transactional
    public Result<Void> clearAllHistory(Long userId) {
        browsingHistoryRepository.deleteByUserId(userId);
        return Result.success();
    }
}
