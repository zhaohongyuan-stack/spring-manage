package com.fengrui.frmanage.dto.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * 新增采购单请求参数。
 */
@Getter
@Setter
@Schema(description = "新增采购单请求参数")
public class AddPurchaseDTO {

    @NotNull(message = "申请部门ID不能为空")
    @Schema(description = "申请部门ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deptId;

    @NotNull(message = "申请人ID不能为空")
    @Schema(description = "申请人用户ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long applyUserId;

    @NotNull(message = "需求日期不能为空")
    @FutureOrPresent(message = "需求日期不能早于当前日期")
    @Schema(description = "需求日期", example = "2026-06-20", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate requireDate;

    @NotNull(message = "供应商ID不能为空")
    @Schema(description = "供应商ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long supplierId;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "6月下旬客房用品补充")
    private String remark;

    @Valid
    @NotEmpty(message = "采购明细不能为空")
    @Schema(description = "采购明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AddPurchaseItemDTO> items;
}
