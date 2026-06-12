package com.fengrui.frmanage.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门表实体。
 */
@Getter
@Setter
@TableName("department")
public class Department {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dept_name")
    private String deptName;

    @TableField("parent_id")
    private Long parentId;

    @TableField("status")
    private Short status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<Department> children;
}
