package com.fengrui.frmanage.dto.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 采购单取消请求参数。
 */
@Getter
@Setter
@Schema(description = "采购单取消请求参数")
public class PurchaseCancelDTO {

    @NotNull(message = "采购单ID不能为空")
    @Schema(description = "【采购单ID】必填，状态1/2/3且未生成入库单时可取消", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long purchaseId;

    @NotNull(message = "操作人用户ID不能为空")
    @Schema(description = "【操作人】必填，申请人或采购员用户ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long operatorUserId;
}
