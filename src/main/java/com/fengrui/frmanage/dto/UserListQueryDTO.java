package com.fengrui.frmanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户列表查询参数。
 */
@Getter
@Setter
@Schema(description = "用户列表查询参数")
public class UserListQueryDTO {

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Schema(description = "真实姓名，支持模糊查询；不传则不按姓名筛选", example = "张伟", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String realName;

    @Size(max = 20, message = "角色长度不能超过20个字符")
    @Schema(description = "角色编码；不传则不按角色筛选", example = "dept_head", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String role;

    @Schema(description = "部门ID；不传则不按部门筛选", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long deptId;
}
