package com.fengrui.frmanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 删除用户请求参数。
 */
@Getter
@Setter
@Schema(description = "删除用户请求参数")
public class DeleteUserDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "【用户ID】必填，逻辑禁用目标用户", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
}
