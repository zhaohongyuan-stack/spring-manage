package com.fengrui.frmanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 新增用户请求参数。
 * API 层使用 account 字段，入库时映射至数据库 username 列；phone 由后端自动填充默认值。
 */
@Getter
@Setter
@Schema(description = "新增用户请求参数")
public class AddUserDTO {

    @NotBlank(message = "登录名不能为空")
    @Size(max = 50, message = "登录名长度不能超过50个字符")
    @Schema(description = "【登录名】必填，全局唯一，建议使用工号或英文名", example = "zhangwei336", requiredMode = Schema.RequiredMode.REQUIRED)
    private String account;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6到50个字符之间")
    @Schema(description = "【密码】必填，前端加密传输", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Schema(description = "【真实姓名】必填，展示用，支持中文", example = "张伟", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @NotBlank(message = "角色不能为空")
    @Size(max = 20, message = "角色长度不能超过20个字符")
    @Schema(description = "【角色】必填，先 GET /api/v1/dict/roles；可用：dept_employee(部门员工)、dept_head(部门负责人)、purchaser(采购员)、warehouse_keeper(仓库管理员)、finance(财务人员)、gm(酒店总经理)（下拉展示中文 name，提交 code）", example = "dept_head", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Schema(description = "【所属部门】部门员工/负责人必填；GET /api/v1/department/tree 下拉选部门（展示 deptName，提交 id）；职能角色可不填", example = "1")
    private Long deptId;
}
