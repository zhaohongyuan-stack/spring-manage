package com.fengrui.frmanage.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 库存初始化明细参数。
 */
@Getter
@Setter
@Schema(description = "库存初始化明细参数")
public class InitInventoryItemDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @NotNull(message = "期初库存数量不能为空")
    @Min(value = 0, message = "期初库存数量不能小于0")
    @Schema(description = "期初库存数量", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer initialQuantity;

    @NotNull(message = "成本单价不能为空")
    @DecimalMin(value = "0.00", message = "成本单价不能小于0")
    @Digits(integer = 14, fraction = 2, message = "成本单价格式不正确")
    @Schema(description = "成本单价", example = "2.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal unitCost;
}
