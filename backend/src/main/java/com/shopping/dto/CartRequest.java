package com.shopping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车请求DTO
 */
@Data
public class CartRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Min(value = 1, message = "数量最少为1")
    private Integer quantity = 1;
}
