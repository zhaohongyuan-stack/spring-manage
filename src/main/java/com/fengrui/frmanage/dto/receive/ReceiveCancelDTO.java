package com.fengrui.frmanage.dto.receive;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 领用单取消请求参数。
 */
@Getter
@Setter
@Schema(description = "领用单取消请求参数")
public class ReceiveCancelDTO {

    @NotNull(message = "领用单ID不能为空")
    @Schema(description = "【领用单ID】必填，状态1/2时可取消", example = "201", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long receiveId;

    @NotNull(message = "操作人用户ID不能为空")
    @Schema(description = "【操作人】必填，申请人用户ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long operatorUserId;
}
