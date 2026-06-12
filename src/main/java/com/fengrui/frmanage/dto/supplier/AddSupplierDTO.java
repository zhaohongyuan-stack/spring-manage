package com.fengrui.frmanage.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 新增供应商请求参数。
 */
@Getter
@Setter
@Schema(description = "新增供应商请求参数")
public class AddSupplierDTO {

    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 200, message = "供应商名称长度不能超过200个字符")
    @Schema(description = "供应商全称", example = "大连XX食品配送中心", requiredMode = Schema.RequiredMode.REQUIRED)
    private String supplierName;

    @Size(max = 50, message = "联系人长度不能超过50个字符")
    @Schema(description = "联系人", example = "李主管")
    private String contactPerson;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Schema(description = "联系电话", example = "13900139000")
    private String contactPhone;

    @Size(max = 500, message = "地址长度不能超过500个字符")
    @Schema(description = "地址", example = "大连市XX区XX路")
    private String address;
}
