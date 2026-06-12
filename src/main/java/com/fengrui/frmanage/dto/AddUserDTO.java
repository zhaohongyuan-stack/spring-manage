package com.fengrui.frmanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 新增用户请求参数。
 */
@Getter
@Setter
@Schema(description = "新增用户请求参数")
public class AddUserDTO {

    @NotBlank(message = "登录账号不能为空")
    @Size(max = 50, message = "登录账号长度不能超过50个字符")
    @Schema(description = "登录账号，唯一", example = "zhangwei", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6到50个字符之间")
    @Schema(description = "登录密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Schema(description = "真实姓名", example = "张伟", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @NotBlank(message = "角色不能为空")
    @Size(max = 20, message = "角色长度不能超过20个字符")
    @Schema(description = "角色编码", example = "dept_head", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Schema(description = "所属部门ID，部门员工和部门负责人必填", example = "1")
    private Long deptId;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Schema(description = "联系电话", example = "13812345678")
    private String phone;
}
