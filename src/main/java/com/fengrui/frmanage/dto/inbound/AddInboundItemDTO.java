package com.fengrui.frmanage.dto.inbound;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 新增入库明细请求参数。
 */
@Getter
@Setter
@Schema(description = "新增入库明细请求参数")
public class AddInboundItemDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @NotNull(message = "实际收货数量不能为空")
    @Min(value = 1, message = "实际收货数量必须大于0")
    @Schema(description = "实际收货数量", example = "98", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer actualQuantity;

    @Size(max = 100, message = "批次号长度不能超过100个字符")
    @Schema(description = "批次号", example = "20260601-B01")
    private String batchNo;

    @Schema(description = "有效期", example = "2027-06-01")
    private LocalDate expiryDate;
}
