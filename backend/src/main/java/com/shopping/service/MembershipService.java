package com.shopping.service;

import com.shopping.dto.MemberLevelRequest;
import com.shopping.dto.Result;
import com.shopping.entity.MemberLevel;
import com.shopping.entity.UserMembership;
import com.shopping.repository.MemberLevelRepository;
import com.shopping.repository.UserMembershipRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final UserMembershipRepository membershipRepository;
    private final MemberLevelRepository memberLevelRepository;
    private final OrderEventQueueService eventQueue;

    @Transactional(rollbackFor = Exception.class)
    public UserMembership initMembership(Long userId) {
        UserMembership existing = membershipRepository.findByUserId(userId).orElse(null);
        if (existing != null) {
            return existing;
        }

        MemberLevel bronzeLevel = memberLevelRepository.findByLevelCode(1).orElse(null);
        Long levelId = bronzeLevel != null ? bronzeLevel.getId() : 1L;

        UserMembership membership = new UserMembership();
        membership.setUserId(userId);
        membership.setLevelId(levelId);
        membership.setTotalPoints(0);
        membership.setTotalEarnedPoints(0);
        membership.setTotalSpending(BigDecimal.ZERO);
        membershipRepository.save(membership);

        log.info("初始化会员信息: userId={}, level=青铜", userId);
        return membership;
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpgrade(Long userId) {
        UserMembership membership = membershipRepository.findByUserIdForUpdate(userId).orElse(null);
        if (membership == null) return;

        List<MemberLevel> levels = memberLevelRepository.findAllByOrderByLevelCodeAsc();
        MemberLevel currentLevel = memberLevelRepository.findById(membership.getLevelId()).orElse(null);
        if (currentLevel == null) return;

        MemberLevel originalLevel = currentLevel;
        MemberLevel upgraded = currentLevel;

        while (true) {
            MemberLevel nextLevel = null;
            for (MemberLevel level : levels) {
                if (level.getLevelCode() == upgraded.getLevelCode() + 1) {
                    nextLevel = level;
                    break;
                }
            }
            if (nextLevel == null) break;

            boolean spendingMet = membership.getTotalSpending().compareTo(nextLevel.getMinSpending()) >= 0;
            boolean pointsMet = membership.getTotalEarnedPoints() >= nextLevel.getMinPoints();
            if (spendingMet && pointsMet) {
                upgraded = nextLevel;
            } else {
                break;
            }
        }

        if (!upgraded.getId().equals(originalLevel.getId())) {
            membership.setLevelId(upgraded.getId());
            membershipRepository.save(membership);
            log.info("会员等级晋升: userId={}, {}({}) -> {}({})",
                    userId, originalLevel.getName(), originalLevel.getLevelCode(),
                    upgraded.getName(), upgraded.getLevelCode());
            eventQueue.publishStatusChangeEvent(
                    "USER:" + userId, originalLevel.getLevelCode(), 100,
                    "system", "会员晋升: " + originalLevel.getName() + " → " + upgraded.getName());
        }
    }

    public Result<MembershipInfoResponse> getMembershipInfo(Long userId) {
        UserMembership membership = membershipRepository.findByUserId(userId).orElse(null);
        if (membership == null) {
            membership = initMembership(userId);
        }

        MemberLevel currentLevel = memberLevelRepository.findById(membership.getLevelId()).orElse(null);
        List<MemberLevel> allLevels = memberLevelRepository.findAllByOrderByLevelCodeAsc();

        MemberLevel nextLevel = null;
        for (MemberLevel level : allLevels) {
            if (currentLevel != null && level.getLevelCode() > currentLevel.getLevelCode()) {
                nextLevel = level;
                break;
            }
        }

        MembershipInfoResponse response = new MembershipInfoResponse();
        response.setUserId(userId);
        response.setTotalPoints(membership.getTotalPoints());
        response.setTotalEarnedPoints(membership.getTotalEarnedPoints());
        response.setTotalSpending(membership.getTotalSpending());

        if (currentLevel != null) {
            response.setLevelName(currentLevel.getName());
            response.setLevelCode(currentLevel.getLevelCode());
            response.setDiscountRate(currentLevel.getDiscountRate());
            response.setPointsMultiplier(currentLevel.getPointsMultiplier());
            response.setLevelIcon(currentLevel.getIcon());
            response.setLevelDescription(currentLevel.getDescription());
        }

        if (nextLevel != null) {
            response.setNextLevelName(nextLevel.getName());
            response.setNextLevelCode(nextLevel.getLevelCode());
            response.setNextLevelMinSpending(nextLevel.getMinSpending());
            response.setNextLevelMinPoints(nextLevel.getMinPoints());
            BigDecimal spendingGap = nextLevel.getMinSpending().subtract(membership.getTotalSpending()).max(BigDecimal.ZERO);
            response.setSpendingToNextLevel(spendingGap);
            response.setPointsToNextLevel(Math.max(0, nextLevel.getMinPoints() - membership.getTotalEarnedPoints()));
        }

        return Result.success(response);
    }

    public Result<List<MemberLevel>> getAllLevels() {
        return Result.success(memberLevelRepository.findAllByOrderByLevelCodeAsc());
    }

    public Result<BigDecimal> getDiscountRate(Long userId) {
        UserMembership membership = membershipRepository.findByUserId(userId).orElse(null);
        if (membership == null) {
            return Result.success(BigDecimal.ONE);
        }
        MemberLevel level = memberLevelRepository.findById(membership.getLevelId()).orElse(null);
        if (level == null) {
            return Result.success(BigDecimal.ONE);
        }
        return Result.success(level.getDiscountRate());
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<MemberLevel> createLevel(MemberLevelRequest request) {
        if (memberLevelRepository.findByLevelCode(request.getLevelCode()).isPresent()) {
            return Result.error(400, "等级编码已存在");
        }
        MemberLevel level = new MemberLevel();
        level.setName(request.getName());
        level.setLevelCode(request.getLevelCode());
        level.setMinSpending(request.getMinSpending());
        level.setMinPoints(request.getMinPoints());
        level.setDiscountRate(request.getDiscountRate());
        level.setPointsMultiplier(request.getPointsMultiplier());
        level.setDescription(request.getDescription());
        level.setIcon(request.getIcon());
        memberLevelRepository.save(level);
        return Result.success(level);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<MemberLevel> updateLevel(Long id, MemberLevelRequest request) {
        MemberLevel level = memberLevelRepository.findById(id).orElse(null);
        if (level == null) {
            return Result.error(404, "等级不存在");
        }
        if (request.getName() != null) level.setName(request.getName());
        if (request.getMinSpending() != null) level.setMinSpending(request.getMinSpending());
        if (request.getMinPoints() != null) level.setMinPoints(request.getMinPoints());
        if (request.getDiscountRate() != null) level.setDiscountRate(request.getDiscountRate());
        if (request.getPointsMultiplier() != null) level.setPointsMultiplier(request.getPointsMultiplier());
        if (request.getDescription() != null) level.setDescription(request.getDescription());
        if (request.getIcon() != null) level.setIcon(request.getIcon());
        memberLevelRepository.save(level);
        return Result.success(level);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteLevel(Long id) {
        MemberLevel level = memberLevelRepository.findById(id).orElse(null);
        if (level == null) {
            return Result.error(404, "等级不存在");
        }
        if (level.getLevelCode() == 1) {
            return Result.error(400, "默认等级不可删除");
        }
        memberLevelRepository.delete(level);
        return Result.success();
    }

    @Data
    public static class MembershipInfoResponse {
        private Long userId;
        private Integer totalPoints;
        private Integer totalEarnedPoints;
        private BigDecimal totalSpending;
        private String levelName;
        private Integer levelCode;
        private BigDecimal discountRate;
        private BigDecimal pointsMultiplier;
        private String levelIcon;
        private String levelDescription;
        private String nextLevelName;
        private Integer nextLevelCode;
        private BigDecimal nextLevelMinSpending;
        private Integer nextLevelMinPoints;
        private BigDecimal spendingToNextLevel;
        private Integer pointsToNextLevel;
    }
}
