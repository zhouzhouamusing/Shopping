package com.shopping.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionRequest {
    private String name;
    private String type;
    private String description;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String productIds;
}
