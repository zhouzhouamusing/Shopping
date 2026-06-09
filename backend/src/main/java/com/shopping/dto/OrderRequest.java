package com.shopping.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {

    private Long addressId;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String remark;

    private List<Long> productIds;
}
