package com.fengrui.frmanage.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 修改商品请求参数。
 */
@Getter
@Setter
@Schema(description = "修改商品请求参数")
public class UpdateProductDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @DecimalMin(value = "0.00", message = "成本单价不能小于0")
    @Digits(integer = 14, fraction = 2, message = "成本单价最多14位整数和2位小数")
    @Schema(description = "成本单价", example = "4.30")
    private BigDecimal costPrice;

    @Min(value = 0, message = "预警阈值不能小于0")
    @Schema(description = "预警阈值", example = "25")
    private Integer warningThreshold;
}
