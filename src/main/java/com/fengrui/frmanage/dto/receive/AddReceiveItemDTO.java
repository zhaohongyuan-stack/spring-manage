package com.fengrui.frmanage.dto.receive;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 新增领用明细请求参数。
 */
@Getter
@Setter
@Schema(description = "新增领用明细请求参数")
public class AddReceiveItemDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @NotNull(message = "领用数量不能为空")
    @Min(value = 1, message = "领用数量必须大于0")
    @Schema(description = "领用数量", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
