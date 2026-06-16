package com.fengrui.frmanage.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购单表实体。
 */
@Getter
@Setter
@TableName("purchase")
public class Purchase {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("purchase_no")
    private String purchaseNo;

    @TableField("dept_id")
    private Long deptId;

    @TableField("apply_user_id")
    private Long applyUserId;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("require_date")
    private LocalDate requireDate;

    @TableField("supplier_id")
    private Long supplierId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("status")
    private Integer status;

    @TableField("approver_id")
    private Long approverId;

    @TableField("approve_time")
    private LocalDateTime approveTime;

    @TableField("purchaser_id")
    private Long purchaserId;

    @TableField("purchase_confirm_time")
    private LocalDateTime purchaseConfirmTime;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<PurchaseItem> items;
}
