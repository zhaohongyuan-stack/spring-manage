package com.fengrui.frmanage.common.enums;

/**
 * 部门状态枚举。
 */
public enum DeptStatusEnum {

    ENABLED(1, "启用");

    private final Integer code;

    private final String name;

    DeptStatusEnum(Integer code, String name) {
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
