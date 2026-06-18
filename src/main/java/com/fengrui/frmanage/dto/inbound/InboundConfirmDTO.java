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
    @Schema(description = "【入库单ID】必填，状态须为1-待验收", example = "301", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long inboundId;

    @NotNull(message = "部门负责人用户ID不能为空")
    @Schema(description = "【部门负责人】必填，须为申请部门的 dept_head", example = "1005", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deptHeadUserId;

    @NotNull(message = "仓库管理员用户ID不能为空")
    @Schema(description = "【仓管员】必填，当前登录仓管用户ID", example = "2001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long warehouseKeeperUserId;
}
