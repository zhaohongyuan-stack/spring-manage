package com.fengrui.frmanage.dto.receive;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 领用确认出库请求参数。
 */
@Getter
@Setter
@Schema(description = "领用确认出库请求参数")
public class ReceiveConfirmDTO {

    @NotNull(message = "领用单ID不能为空")
    @Schema(description = "领用单ID", example = "201", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long receiveId;

    @NotNull(message = "出库发货人用户ID不能为空")
    @Schema(description = "出库发货人用户ID", example = "1010", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long delivererUserId;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "已全部发货")
    private String remark;
}
