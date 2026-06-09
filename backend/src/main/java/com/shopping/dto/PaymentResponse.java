package com.shopping.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {

    private String paymentNo;
    private String orderNo;
    private String paymentMethod;
    private BigDecimal paymentAmount;
    private String paymentStatus;
    private LocalDateTime expireTime;
    private Long remainingSeconds;
    private LocalDateTime payTime;
}
