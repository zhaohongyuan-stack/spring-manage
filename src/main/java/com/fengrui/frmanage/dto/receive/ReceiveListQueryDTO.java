package com.fengrui.frmanage.dto.receive;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 领用单分页查询参数。
 */
@Getter
@Setter
@Schema(description = "领用单分页查询参数")
public class ReceiveListQueryDTO {

    @Schema(description = "【页码】必填，从1开始", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Schema(description = "【每页条数】必填，默认10", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "【领用单状态】可选，1待审批/2待出库/3已出库/4已取消/5已驳回", example = "1")
    private Integer status;

    @Schema(description = "【部门ID】可选，精确匹配", example = "1")
    private Long deptId;

    @Schema(description = "【申请人】可选，用户ID精确匹配", example = "1001")
    private Long applyUserId;

    @Schema(description = "【开始时间】可选，申请时间区间起点", example = "2026-06-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "【结束时间】可选，申请时间区间终点", example = "2026-06-10T23:59:59")
    private LocalDateTime endTime;
}
