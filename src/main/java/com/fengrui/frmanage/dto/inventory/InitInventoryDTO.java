package com.fengrui.frmanage.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 库存初始化请求参数。
 */
@Getter
@Setter
@Schema(description = "库存初始化请求参数")
public class InitInventoryDTO {

    @NotNull(message = "操作人ID不能为空")
    @Schema(description = "操作人用户ID，当前 admin 模式临时从请求体传入", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long operatorUserId;

    @Valid
    @NotEmpty(message = "初始化明细不能为空")
    @Schema(description = "初始化明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<InitInventoryItemDTO> items;
}
