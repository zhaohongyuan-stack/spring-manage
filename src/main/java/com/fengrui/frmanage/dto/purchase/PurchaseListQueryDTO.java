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

    @Schema(description = "【页码】必填，从1开始", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Schema(description = "【每页条数】必填，默认10", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "【操作人】必填，当前登录用户ID（后续用于权限过滤）", example = "1")
    private Long operatorUserId;

    @Schema(description = "【采购单状态】可选，1待审批/2已通过/3采购中/4已入库/5已取消/6已驳回", example = "1")
    private Integer status;

    @Schema(description = "【采购单号】可选，模糊匹配", example = "CG-20260612")
    private String purchaseNo;
}
