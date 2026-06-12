package com.fengrui.frmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 领用明细表实体。
 */
@Getter
@Setter
@TableName("receive_item")
public class ReceiveItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("receive_id")
    private Long receiveId;

    @TableField("product_id")
    private Long productId;

    @TableField("product_name")
    private String productName;

    @TableField("quantity")
    private Integer quantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("amount")
    private BigDecimal amount;
}
