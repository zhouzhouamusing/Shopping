package com.shopping.dto;

import lombok.Data;

@Data
public class PointsAdjustRequest {

    private Long userId;

    private Integer points;

    private String reason;
}
