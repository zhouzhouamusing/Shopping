package com.shopping.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CategoryPointsRuleRequest {

    private Long categoryId;

    private BigDecimal pointsRate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
