package com.fengrui.frmanage.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户列表响应数据。
 * API 层使用 account 字段展示登录名，对应数据库 username 列。
 */
@Getter
@Setter
@Schema(description = "用户列表响应数据")
public class UserListVO {

    @Schema(description = "【用户ID】主键", example = "101")
    private Long userId;

    @Schema(description = "【登录名】全局唯一", example = "zhangwei336")
    private String account;

    @Schema(description = "【真实姓名】展示用", example = "张伟")
    private String realName;

    @Schema(description = "【角色编码】与 dict/roles 一致", example = "dept_head")
    private String role;

    @Schema(description = "【角色名称】中文展示", example = "部门负责人")
    private String roleName;

    @Schema(description = "【部门ID】职能角色可能为空", example = "1")
    private Long deptId;

    @Schema(description = "【部门名称】中文展示", example = "客房部")
    private String deptName;

    @Schema(description = "【联系电话】系统默认占位，前端可不展示", example = "186XXXX9999")
    private String phone;

    @Schema(description = "【用户状态】1启用，0禁用", example = "1")
    private Short status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "【创建时间】", example = "2026-06-10 09:00:00")
    private LocalDateTime createTime;
}
