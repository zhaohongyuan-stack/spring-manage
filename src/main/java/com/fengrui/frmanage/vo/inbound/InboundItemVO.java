package com.fengrui.frmanage.vo.inbound;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 入库明细响应数据。
 */
@Getter
@Setter
@Schema(description = "入库明细响应数据")
public class InboundItemVO {

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "一次性牙具套装")
    private String productName;

    @Schema(description = "采购单数量", example = "100")
    private Integer orderQuantity;

    @Schema(description = "实际收货数量", example = "98")
    private Integer actualQuantity;

    @Schema(description = "采购单价", example = "2.20")
    private BigDecimal unitPrice;

    @Schema(description = "金额", example = "215.60")
    private BigDecimal amount;

    @Schema(description = "批次号", example = "20260601-B01")
    private String batchNo;

    @Schema(description = "有效期", example = "2027-06-01")
    private LocalDate expiryDate;
}
