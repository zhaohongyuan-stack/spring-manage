package com.fengrui.frmanage.dto.inbound;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 入库单分页查询参数。
 */
@Getter
@Setter
@Schema(description = "入库单分页查询参数")
public class InboundListQueryDTO {

    @Schema(description = "【页码】必填，从1开始", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Schema(description = "【每页条数】必填，默认10", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "【入库类型】可选，1常规入库/2食材直拨", example = "1")
    private Integer inboundType;

    @Schema(description = "【入库单状态】可选，1待验收/2已验收", example = "1")
    private Integer status;

    @Schema(description = "【采购单号】可选，模糊匹配", example = "CG-202606")
    private String purchaseNo;
}
