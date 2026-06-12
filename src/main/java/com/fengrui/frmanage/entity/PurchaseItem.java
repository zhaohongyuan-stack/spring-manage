package com.fengrui.frmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 采购明细表实体。
 */
@Getter
@Setter
@TableName("purchase_item")
public class PurchaseItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("purchase_id")
    private Long purchaseId;

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

    @TableField("received_quantity")
    private Integer receivedQuantity;
}
