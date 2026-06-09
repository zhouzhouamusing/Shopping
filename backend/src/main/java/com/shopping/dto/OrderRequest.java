package com.shopping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
public class OrderRequest {

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    private String remark;

    /** 指定商品ID列表，为空则使用购物车中已选中的商品 */
    private List<Long> productIds;
}
