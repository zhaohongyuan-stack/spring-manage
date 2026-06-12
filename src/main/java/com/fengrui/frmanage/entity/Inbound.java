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
import java.util.List;

/**
 * 入库单表实体。
 */
@Getter
@Setter
@TableName("inbound")
public class Inbound {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("inbound_no")
    private String inboundNo;

    @TableField("purchase_id")
    private Long purchaseId;

    @TableField("supplier_id")
    private Long supplierId;

    @TableField("receiver_id")
    private Long receiverId;

    @TableField("inbound_type")
    private Short inboundType;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("status")
    private Short status;

    @TableField("dept_head_id")
    private Long deptHeadId;

    @TableField("accept_time")
    private LocalDateTime acceptTime;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<InboundItem> items;
}
