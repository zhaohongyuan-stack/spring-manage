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

    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "领用单状态", example = "1")
    private Integer status;

    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    @Schema(description = "申请人用户ID", example = "1001")
    private Long applyUserId;

    @Schema(description = "开始时间", example = "2026-06-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-06-10T23:59:59")
    private LocalDateTime endTime;
}
