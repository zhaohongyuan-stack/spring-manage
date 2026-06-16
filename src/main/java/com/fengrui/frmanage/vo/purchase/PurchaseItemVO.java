package com.fengrui.frmanage.vo.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 采购明细响应数据。
 */
@Getter
@Setter
@Schema(description = "采购明细响应数据")
public class PurchaseItemVO {

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "一次性牙具套装")
    private String productName;

    @Schema(description = "采购数量", example = "100")
    private Integer quantity;

    @Schema(description = "采购单价", example = "2.20")
    private BigDecimal unitPrice;

    @Schema(description = "小计金额", example = "220.00")
    private BigDecimal amount;

    @Schema(description = "已收货数量", example = "0")
    private Integer receivedQuantity;
}
