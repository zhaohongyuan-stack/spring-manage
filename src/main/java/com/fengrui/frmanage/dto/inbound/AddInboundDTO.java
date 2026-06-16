package com.fengrui.frmanage.dto.inbound;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 新增入库单请求参数。
 */
@Getter
@Setter
@Schema(description = "新增入库单请求参数")
public class AddInboundDTO {

    @NotNull(message = "采购单ID不能为空")
    @Schema(description = "采购单ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long purchaseId;

    @NotNull(message = "仓库管理员用户ID不能为空")
    @Schema(description = "仓库管理员用户ID", example = "2001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long receiverUserId;

    @NotNull(message = "入库类型不能为空")
    @Schema(description = "入库类型：1常规入库，2食材直拨", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer inboundType;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "外包装损坏2套，已退回")
    private String remark;

    @Valid
    @NotEmpty(message = "入库明细不能为空")
    @Schema(description = "入库明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AddInboundItemDTO> items;
}
