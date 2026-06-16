package com.fengrui.frmanage.dto.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 采购单分页查询参数。
 */
@Getter
@Setter
@Schema(description = "采购单分页查询参数")
public class PurchaseListQueryDTO {

    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "当前操作人用户ID，本阶段仅接收不做角色权限过滤", example = "1")
    private Long operatorUserId;

    @Schema(description = "采购单状态", example = "1")
    private Integer status;

    @Schema(description = "采购单号，支持模糊查询", example = "CG-20260612")
    private String purchaseNo;
}
