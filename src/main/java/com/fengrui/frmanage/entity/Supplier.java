package com.fengrui.frmanage.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 供应商表实体。
 */
@Getter
@Setter
@TableName("supplier")
public class Supplier {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("supplier_name")
    private String supplierName;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("address")
    private String address;

    @TableField("status")
    private Short status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
