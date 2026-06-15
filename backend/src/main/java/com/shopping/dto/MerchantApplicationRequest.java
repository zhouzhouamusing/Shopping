package com.shopping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MerchantApplicationRequest {

    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    private String businessLicense;

    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String description;
}
