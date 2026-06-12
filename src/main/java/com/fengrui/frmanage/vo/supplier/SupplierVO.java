package com.fengrui.frmanage.vo.supplier;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 供应商列表响应数据。
 */
@Getter
@Setter
@Schema(description = "供应商列表响应数据")
public class SupplierVO {

    @Schema(description = "供应商ID", example = "1")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "沈阳XX酒店用品有限公司")
    private String supplierName;

    @Schema(description = "联系人", example = "王经理")
    private String contactPerson;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "地址", example = "沈阳市XX区")
    private String address;

    @Schema(description = "状态：1启用，0禁用", example = "1")
    private Short status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2026-06-01 09:00:00")
    private LocalDateTime createTime;
}
