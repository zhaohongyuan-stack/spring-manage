package com.fengrui.frmanage.common.enums;

/**
 * 供应商状态枚举。
 */
public enum SupplierStatusEnum {

    ENABLED(1, "启用");

    private final Integer code;

    private final String name;

    SupplierStatusEnum(Integer code, String name) {
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
