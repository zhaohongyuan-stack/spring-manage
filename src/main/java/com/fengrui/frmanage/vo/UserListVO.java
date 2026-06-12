package com.fengrui.frmanage.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户列表响应数据。
 */
@Getter
@Setter
@Schema(description = "用户列表响应数据")
public class UserListVO {

    @Schema(description = "用户ID", example = "101")
    private Long userId;

    @Schema(description = "登录账号", example = "zhangwei")
    private String username;

    @Schema(description = "真实姓名", example = "张伟")
    private String realName;

    @Schema(description = "角色编码", example = "dept_head")
    private String role;

    @Schema(description = "角色名称", example = "部门负责人")
    private String roleName;

    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    @Schema(description = "部门名称", example = "客房部")
    private String deptName;

    @Schema(description = "联系电话", example = "13812345678")
    private String phone;

    @Schema(description = "用户状态：1启用，0禁用", example = "1")
    private Short status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2026-06-10 09:00:00")
    private LocalDateTime createTime;
}
