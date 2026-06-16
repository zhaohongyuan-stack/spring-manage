package com.fengrui.frmanage.dto.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 新增采购明细请求参数。
 */
@Getter
@Setter
@Schema(description = "新增采购明细请求参数")
public class AddPurchaseItemDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @NotNull(message = "采购数量不能为空")
    @Min(value = 1, message = "采购数量必须大于0")
    @Schema(description = "采购数量", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @NotNull(message = "采购单价不能为空")
    @DecimalMin(value = "0.00", inclusive = false, message = "采购单价必须大于0")
    @Digits(integer = 14, fraction = 2, message = "采购单价最多14位整数和2位小数")
    @Schema(description = "采购单价", example = "2.20", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal unitPrice;
}
