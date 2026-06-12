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
 * 库存流水表实体。
 */
@Getter
@Setter
@TableName("inventory_record")
public class InventoryRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("product_name")
    private String productName;

    @TableField("change_type")
    private Short changeType;

    @TableField("change_quantity")
    private Integer changeQuantity;

    @TableField("before_quantity")
    private Integer beforeQuantity;

    @TableField("after_quantity")
    private Integer afterQuantity;

    @TableField("related_no")
    private String relatedNo;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
