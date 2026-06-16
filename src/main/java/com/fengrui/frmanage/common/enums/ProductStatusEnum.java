package com.fengrui.frmanage.common.enums;

/**
 * 商品状态枚举。
 */
public enum ProductStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;

    private final String name;

    ProductStatusEnum(Integer code, String name) {
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
