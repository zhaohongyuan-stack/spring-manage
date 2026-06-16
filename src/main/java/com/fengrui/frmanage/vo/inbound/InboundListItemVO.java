package com.fengrui.frmanage.vo.inbound;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单列表项响应数据。
 */
@Getter
@Setter
@Schema(description = "入库单列表项响应数据")
public class InboundListItemVO {

    @Schema(description = "入库单ID", example = "301")
    private Long inboundId;

    @Schema(description = "入库单号", example = "RK-20260610-001")
    private String inboundNo;

    @Schema(description = "采购单ID", example = "101")
    private Long purchaseId;

    @Schema(description = "采购单号", example = "CG-20260610-001")
    private String purchaseNo;

    @Schema(description = "供应商名称", example = "沈阳XX酒店用品公司")
    private String supplierName;

    @Schema(description = "收货人姓名", example = "李四")
    private String receiverName;

    @Schema(description = "入库类型", example = "1")
    private Integer inboundType;

    @Schema(description = "入库类型名称", example = "常规入库")
    private String inboundTypeName;

    @Schema(description = "总金额", example = "430.60")
    private BigDecimal totalAmount;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "状态名称", example = "待验收")
    private String statusName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2026-06-10 14:00:00")
    private LocalDateTime createTime;
}
