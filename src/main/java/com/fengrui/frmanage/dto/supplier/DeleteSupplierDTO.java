package com.fengrui.frmanage.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 删除供应商请求参数。
 */
@Getter
@Setter
@Schema(description = "删除供应商请求参数")
public class DeleteSupplierDTO {

    @NotNull(message = "供应商ID不能为空")
    @Schema(description = "【供应商ID】必填，已被采购单引用时不可删", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
}
