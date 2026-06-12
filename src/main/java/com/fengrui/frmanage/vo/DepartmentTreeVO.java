package com.fengrui.frmanage.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 部门树形响应数据。
 */
@Getter
@Setter
@Schema(description = "部门树形响应数据")
public class DepartmentTreeVO {

    @Schema(description = "部门ID", example = "1")
    private Long id;

    @Schema(description = "部门名称", example = "客房部")
    private String deptName;

    @Schema(description = "上级部门ID", example = "0")
    private Long parentId;

    @TableField(exist = false)
    @Schema(description = "子部门列表")
    private List<DepartmentTreeVO> children;
}
