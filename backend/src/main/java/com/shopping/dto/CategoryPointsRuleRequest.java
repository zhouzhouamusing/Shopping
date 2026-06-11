package com.shopping.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryPointsRuleRequest {

    private Long categoryId;

    private BigDecimal pointsRate;
}
