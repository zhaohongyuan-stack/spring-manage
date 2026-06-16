package com.fengrui.frmanage.vo.receive;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 领用明细响应数据。
 */
@Getter
@Setter
@Schema(description = "领用明细响应数据")
public class ReceiveItemVO {

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "一次性牙具套装")
    private String productName;

    @Schema(description = "领用数量", example = "50")
    private Integer quantity;

    @Schema(description = "单价快照", example = "2.20")
    private BigDecimal unitPrice;

    @Schema(description = "小计金额", example = "110.00")
    private BigDecimal amount;
}
