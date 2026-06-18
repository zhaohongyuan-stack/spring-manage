package com.fengrui.frmanage.dto.receive;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 领用单审批请求参数。
 */
@Getter
@Setter
@Schema(description = "领用单审批请求参数")
public class ReceiveApproveDTO {

    @NotNull(message = "领用单ID不能为空")
    @Schema(description = "【领用单ID】必填", example = "201", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long receiveId;

    @NotNull(message = "审批人用户ID不能为空")
    @Schema(description = "【审批人】必填，当前登录的部门负责人用户ID", example = "1005", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long approverUserId;

    @NotNull(message = "审批结果不能为空")
    @Schema(description = "【审批结果】必填，true通过/false驳回", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean approved;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "【审批备注】驳回时必填", example = "同意领用")
    private String remark;
}
