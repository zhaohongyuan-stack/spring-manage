package com.fengrui.frmanage.dto.receive;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 新增领用单请求参数。
 */
@Getter
@Setter
@Schema(description = "新增领用单请求参数")
public class AddReceiveDTO {

    @NotNull(message = "领用部门ID不能为空")
    @Schema(description = "【领用部门】必填，须与申请人所属部门一致", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deptId;

    @NotNull(message = "申请人ID不能为空")
    @Schema(description = "【申请人】必填，当前登录用户ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long applyUserId;

    @NotBlank(message = "用途说明不能为空")
    @Size(max = 200, message = "用途说明长度不能超过200个字符")
    @Schema(description = "【用途说明】必填，如3楼客房补充", example = "3楼客房一次性用品补充", requiredMode = Schema.RequiredMode.REQUIRED)
    private String purpose;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "【备注】可选", example = "急需，请尽快审批")
    private String remark;

    @Valid
    @NotEmpty(message = "领用明细不能为空")
    @Schema(description = "【领用明细】必填，至少一条", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AddReceiveItemDTO> items;
}
