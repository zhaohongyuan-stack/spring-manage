package com.fengrui.frmanage.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存表实体。
 */
@Getter
@Setter
@TableName("inventory")
public class Inventory {

    @TableId("product_id")
    private Long productId;

    @TableField("stock_quantity")
    private Integer stockQuantity;

    @TableField("unit_cost")
    private BigDecimal unitCost;

    @TableField("total_cost")
    private BigDecimal totalCost;

    @TableField("last_update_time")
    private LocalDateTime lastUpdateTime;
}
