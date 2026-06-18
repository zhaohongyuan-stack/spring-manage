package com.fengrui.frmanage.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 供应商分页查询参数。
 */
@Getter
@Setter
@Schema(description = "供应商分页查询参数")
public class SupplierQueryDTO {

    @Schema(description = "【页码】必填，从1开始", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Schema(description = "【每页条数】必填，默认10", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "【供应商名称】可选，模糊匹配", example = "食品")
    private String supplierName;
}
