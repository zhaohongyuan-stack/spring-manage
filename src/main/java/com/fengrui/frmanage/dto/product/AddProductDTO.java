package com.fengrui.frmanage.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 新增商品请求参数。
 */
@Getter
@Setter
@Schema(description = "新增商品请求参数")
public class AddProductDTO {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200个字符")
    @Schema(description = "商品名称", example = "瓶装洗发水", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    @NotBlank(message = "商品分类不能为空")
    @Size(max = 50, message = "商品分类长度不能超过50个字符")
    @Schema(description = "商品分类", example = "客房用品", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @Size(max = 200, message = "规格型号长度不能超过200个字符")
    @Schema(description = "规格型号", example = "300ml/瓶")
    private String spec;

    @Size(max = 20, message = "单位长度不能超过20个字符")
    @Schema(description = "单位", example = "瓶")
    private String unit;

    @NotNull(message = "成本单价不能为空")
    @DecimalMin(value = "0.00", message = "成本单价不能小于0")
    @Digits(integer = 14, fraction = 2, message = "成本单价最多14位整数和2位小数")
    @Schema(description = "成本单价", example = "4.10", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal costPrice;

    @NotNull(message = "预警阈值不能为空")
    @Min(value = 0, message = "预警阈值不能小于0")
    @Schema(description = "预警阈值", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer warningThreshold;
}
