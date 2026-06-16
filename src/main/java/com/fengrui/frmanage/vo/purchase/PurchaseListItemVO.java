package com.fengrui.frmanage.vo.purchase;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购单列表项响应数据。
 */
@Getter
@Setter
@Schema(description = "采购单列表项响应数据")
public class PurchaseListItemVO {

    @Schema(description = "采购单ID", example = "101")
    private Long purchaseId;

    @Schema(description = "采购单号", example = "CG-20260610-001")
    private String purchaseNo;

    @Schema(description = "申请部门ID", example = "1")
    private Long deptId;

    @Schema(description = "申请部门名称", example = "客房部")
    private String deptName;

    @Schema(description = "申请人用户ID", example = "1001")
    private Long applyUserId;

    @Schema(description = "申请人真实姓名", example = "张三")
    private String applyUserRealName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "申请时间", example = "2026-06-10 09:00:00")
    private LocalDateTime applyTime;

    @Schema(description = "需求日期", example = "2026-06-20")
    private LocalDate requireDate;

    @Schema(description = "供应商ID", example = "3")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "沈阳XX酒店用品公司")
    private String supplierName;

    @Schema(description = "采购总金额", example = "475.00")
    private BigDecimal totalAmount;

    @Schema(description = "采购状态", example = "2")
    private Integer status;

    @Schema(description = "采购状态名称", example = "已通过")
    private String statusName;
}
