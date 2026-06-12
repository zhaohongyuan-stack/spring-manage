package com.fengrui.frmanage.common.enums;

/**
 * 入库类型枚举。
 */
public enum InboundTypeEnum {

    NORMAL(1, "常规入库"),
    DIRECT_FOOD(2, "食材直拨");

    private final Integer code;

    private final String name;

    InboundTypeEnum(Integer code, String name) {
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
