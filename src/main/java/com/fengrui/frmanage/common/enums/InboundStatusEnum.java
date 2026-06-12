package com.fengrui.frmanage.common.enums;

/**
 * 入库单状态枚举。
 */
public enum InboundStatusEnum {

    PENDING_ACCEPTANCE(1, "待验收"),
    ACCEPTED(2, "已验收");

    private final Integer code;

    private final String name;

    InboundStatusEnum(Integer code, String name) {
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
