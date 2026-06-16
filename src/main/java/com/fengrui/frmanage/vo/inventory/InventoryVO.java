package com.fengrui.frmanage.vo.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 当前库存响应数据。
 */
@Getter
@Setter
@Schema(description = "当前库存响应数据")
public class InventoryVO {

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

    @Schema(description = "库存数量", example = "100")
    private Integer stockQuantity;

    @Schema(description = "成本单价", example = "2.50")
    private BigDecimal unitCost;

    @Schema(description = "库存总金额", example = "250.00")
    private BigDecimal totalCost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后更新时间", example = "2026-06-16 10:00:00")
    private LocalDateTime lastUpdateTime;
}
