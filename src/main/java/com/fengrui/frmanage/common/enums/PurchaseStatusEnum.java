package com.fengrui.frmanage.common.enums;

/**
 * 采购单状态枚举。
 */
public enum PurchaseStatusEnum {

    PENDING_APPROVAL(1, "待审批"),
    APPROVED(2, "已通过"),
    PURCHASING(3, "采购中"),
    INBOUNDED(4, "已入库"),
    CANCELLED(5, "已取消"),
    REJECTED(6, "已驳回");

    private final Integer code;

    private final String name;

    PurchaseStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
