package com.fengrui.frmanage.vo.dict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 角色字典项，供前端下拉展示与提交。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色字典项")
public class RoleDictVO {

    @Schema(description = "【角色编码】提交时使用", example = "dept_head")
    private String code;

    @Schema(description = "【角色名称】下拉展示中文", example = "部门负责人")
    private String name;
}
