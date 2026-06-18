package com.fengrui.frmanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 新增部门请求参数。
 */
@Getter
@Setter
@Schema(description = "新增部门请求参数")
public class AddDepartmentDTO {

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    @Schema(description = "【部门名称】必填，如客房部、餐饮部", example = "会议宴会部", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deptName;

    @NotNull(message = "上级部门ID不能为空")
    @Schema(description = "【上级部门ID】必填，顶级部门传0", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parentId;
}
