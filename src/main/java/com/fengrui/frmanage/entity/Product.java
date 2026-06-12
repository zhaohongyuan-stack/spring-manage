package com.fengrui.frmanage.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品表实体。
 */
@Getter
@Setter
@TableName("product")
public class Product {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("product_name")
    private String productName;

    @TableField("category")
    private String category;

    @TableField("spec")
    private String spec;

    @TableField("unit")
    private String unit;

    @TableField("cost_price")
    private BigDecimal costPrice;

    @TableField("warning_threshold")
    private Integer warningThreshold;

    @TableField("status")
    private Short status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
