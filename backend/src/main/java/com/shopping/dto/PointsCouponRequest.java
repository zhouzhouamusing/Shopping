package com.shopping.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PointsCouponRequest {

    private String name;

    private Integer pointsCost;

    private String couponType;

    private BigDecimal couponValue;

    private BigDecimal minOrderAmount;

    private Integer validDays;

    private Integer totalStock;

    private Integer status;
}
