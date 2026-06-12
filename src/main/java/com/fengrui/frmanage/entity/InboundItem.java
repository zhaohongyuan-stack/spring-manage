package com.fengrui.frmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 入库明细表实体。
 */
@Getter
@Setter
@TableName("inbound_item")
public class InboundItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("inbound_id")
    private Long inboundId;

    @TableField("product_id")
    private Long productId;

    @TableField("product_name")
    private String productName;

    @TableField("order_quantity")
    private Integer orderQuantity;

    @TableField("actual_quantity")
    private Integer actualQuantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("batch_no")
    private String batchNo;

    @TableField("expiry_date")
    private LocalDate expiryDate;
}
