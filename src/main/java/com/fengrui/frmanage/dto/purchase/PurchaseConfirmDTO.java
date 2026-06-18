package com.fengrui.frmanage.dto.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 采购确认请求参数。
 */
@Getter
@Setter
@Schema(description = "采购确认请求参数")
public class PurchaseConfirmDTO {

    @NotNull(message = "采购单ID不能为空")
    @Schema(description = "【采购单ID】必填，状态须为2-已通过", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long purchaseId;

    @NotNull(message = "采购员用户ID不能为空")
    @Schema(description = "【采购员】必填，当前登录采购员用户ID", example = "1008", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long purchaserUserId;
}
