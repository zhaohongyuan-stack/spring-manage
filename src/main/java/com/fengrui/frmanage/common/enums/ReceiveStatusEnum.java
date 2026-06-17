package com.fengrui.frmanage.common.enums;

import java.util.Arrays;

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

    /**
     * 根据状态编码获取枚举。
     *
     * @param code 状态编码
     * @return 领用状态枚举，不存在时返回 null
     */
    public static ReceiveStatusEnum getByCode(Integer code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据状态编码获取中文名称。
     *
     * @param code 状态编码
     * @return 中文名称，不存在时返回原编码文本
     */
    public static String getNameByCode(Integer code) {
        ReceiveStatusEnum statusEnum = getByCode(code);
        return statusEnum == null ? String.valueOf(code) : statusEnum.getName();
    }
}
