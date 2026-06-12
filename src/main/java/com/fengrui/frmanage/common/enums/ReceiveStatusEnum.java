package com.fengrui.frmanage.common.enums;

/**
 * 领用单状态枚举。
 */
public enum ReceiveStatusEnum {

    PENDING_APPROVAL(1, "待审批"),
    PENDING_OUTBOUND(2, "待出库"),
    OUTBOUNDED(3, "已出库"),
    CANCELLED(4, "已取消"),
    REJECTED(5, "已驳回");

    private final Integer code;

    private final String name;

    ReceiveStatusEnum(Integer code, String name) {
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
