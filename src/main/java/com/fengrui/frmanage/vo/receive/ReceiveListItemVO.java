package com.fengrui.frmanage.vo.receive;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 领用单列表项响应数据。
 */
@Getter
@Setter
@Schema(description = "领用单列表项响应数据")
public class ReceiveListItemVO {

    @Schema(description = "领用单ID", example = "201")
    private Long receiveId;

    @Schema(description = "领用单号", example = "LY-20260610-001")
    private String receiveNo;

    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    @Schema(description = "部门名称", example = "客房部")
    private String deptName;

    @Schema(description = "申请人用户ID", example = "1001")
    private Long applyUserId;

    @Schema(description = "申请人真实姓名", example = "张三")
    private String applyUserRealName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "申请时间", example = "2026-06-10 09:00:00")
    private LocalDateTime applyTime;

    @Schema(description = "用途说明", example = "3楼客房一次性用品补充")
    private String purpose;

    @Schema(description = "领用总金额", example = "196.00")
    private BigDecimal totalAmount;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "状态名称", example = "待出库")
    private String statusName;
}
