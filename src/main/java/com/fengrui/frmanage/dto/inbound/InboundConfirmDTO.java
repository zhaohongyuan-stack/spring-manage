package com.fengrui.frmanage.dto.inbound;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 入库验收确认请求参数。
 */
@Getter
@Setter
@Schema(description = "入库验收确认请求参数")
public class InboundConfirmDTO {

    @NotNull(message = "入库单ID不能为空")
    @Schema(description = "入库单ID", example = "301", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long inboundId;

    @NotNull(message = "部门负责人用户ID不能为空")
    @Schema(description = "部门负责人用户ID", example = "1005", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deptHeadUserId;

    @NotNull(message = "仓库管理员用户ID不能为空")
    @Schema(description = "仓库管理员用户ID", example = "2001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long warehouseKeeperUserId;
}
