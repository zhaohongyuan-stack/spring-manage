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
 * 领用单表实体。
 */
@Getter
@Setter
@TableName("receive")
public class Receive {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("receive_no")
    private String receiveNo;

    @TableField("dept_id")
    private Long deptId;

    @TableField("apply_user_id")
    private Long applyUserId;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("purpose")
    private String purpose;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("status")
    private Short status;

    @TableField("approver_id")
    private Long approverId;

    @TableField("approve_time")
    private LocalDateTime approveTime;

    @TableField("deliverer_id")
    private Long delivererId;

    @TableField("deliver_time")
    private LocalDateTime deliverTime;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<ReceiveItem> items;
}
