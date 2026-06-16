package com.fengrui.frmanage.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 库存流水分页查询参数。
 */
@Getter
@Setter
@Schema(description = "库存流水分页查询参数")
public class InventoryRecordQueryDTO {

    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "库存变动类型，1正常入库，2正常出库，3食材直拨，4期初，5盘点", example = "2")
    private Integer changeType;

    @Schema(description = "开始时间", example = "2026-06-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-06-10T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "关联单号，支持模糊匹配", example = "LY-202606")
    private String relatedNo;
}
