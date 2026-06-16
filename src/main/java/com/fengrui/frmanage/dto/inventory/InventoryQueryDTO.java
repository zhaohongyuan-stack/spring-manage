package com.fengrui.frmanage.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 库存分页查询参数。
 */
@Getter
@Setter
@Schema(description = "库存分页查询参数")
public class InventoryQueryDTO {

    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "商品名称，支持模糊查询", example = "牙具")
    private String productName;

    @Schema(description = "商品分类，精确匹配", example = "客房用品")
    private String category;
}
