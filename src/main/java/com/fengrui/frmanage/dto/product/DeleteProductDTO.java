package com.fengrui.frmanage.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 删除商品请求参数。
 */
@Getter
@Setter
@Schema(description = "删除商品请求参数")
public class DeleteProductDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "【商品ID】必填，逻辑删除", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;
}
