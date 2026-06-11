package com.shopping.config;

import com.shopping.entity.MemberLevel;
import com.shopping.repository.MemberLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipDataInitializer implements CommandLineRunner {

    private final MemberLevelRepository memberLevelRepository;

    @Override
    public void run(String... args) {
        if (memberLevelRepository.count() > 0) {
            return;
        }

        List<MemberLevel> levels = List.of(
                createLevel("青铜会员", 1, BigDecimal.ZERO, 0,
                        new BigDecimal("1.00"), new BigDecimal("1.00"), "基础会员等级"),
                createLevel("白银会员", 2, new BigDecimal("1000.00"), 1000,
                        new BigDecimal("0.98"), new BigDecimal("1.20"), "累计消费满1000元或积分满1000"),
                createLevel("黄金会员", 3, new BigDecimal("5000.00"), 5000,
                        new BigDecimal("0.95"), new BigDecimal("1.50"), "累计消费满5000元或积分满5000"),
                createLevel("铂金会员", 4, new BigDecimal("20000.00"), 20000,
                        new BigDecimal("0.92"), new BigDecimal("2.00"), "累计消费满20000元或积分满20000"),
                createLevel("钻石会员", 5, new BigDecimal("50000.00"), 50000,
                        new BigDecimal("0.88"), new BigDecimal("3.00"), "累计消费满50000元或积分满50000")
        );

        memberLevelRepository.saveAll(levels);
        log.info("初始化会员等级数据完成: {}个等级", levels.size());
    }

    private MemberLevel createLevel(String name, int levelCode, BigDecimal minSpending,
                                     int minPoints, BigDecimal discountRate,
                                     BigDecimal pointsMultiplier, String description) {
        MemberLevel level = new MemberLevel();
        level.setName(name);
        level.setLevelCode(levelCode);
        level.setMinSpending(minSpending);
        level.setMinPoints(minPoints);
        level.setDiscountRate(discountRate);
        level.setPointsMultiplier(pointsMultiplier);
        level.setDescription(description);
        return level;
    }
}
