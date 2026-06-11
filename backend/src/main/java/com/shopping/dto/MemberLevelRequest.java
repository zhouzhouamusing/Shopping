package com.shopping.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MemberLevelRequest {

    private String name;

    private Integer levelCode;

    private BigDecimal minSpending;

    private Integer minPoints;

    private BigDecimal discountRate;

    private BigDecimal pointsMultiplier;

    private String description;

    private String icon;
}
