package com.fengrui.frmanage.vo.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 库存预警响应数据。
 */
@Getter
@Setter
@Schema(description = "库存预警响应数据")
public class InventoryWarningVO {

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "一次性牙具套装")
    private String productName;

    @Schema(description = "商品分类", example = "客房用品")
    private String category;

    @Schema(description = "规格", example = "套")
    private String spec;

    @Schema(description = "单位", example = "套")
    private String unit;

    @Schema(description = "预警阈值", example = "50")
    private Integer warningThreshold;

    @Schema(description = "当前库存", example = "20")
    private Integer stockQuantity;

    @Schema(description = "建议采购量", example = "80")
    private Integer suggestQuantity;
}
