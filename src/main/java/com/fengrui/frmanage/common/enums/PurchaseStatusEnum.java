package com.fengrui.frmanage.common.enums;

import java.util.Arrays;

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

    /**
     * 根据状态编码获取枚举。
     *
     * @param code 状态编码
     * @return 采购状态枚举，不存在时返回 null
     */
    public static PurchaseStatusEnum getByCode(Integer code) {
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
        PurchaseStatusEnum statusEnum = getByCode(code);
        return statusEnum == null ? String.valueOf(code) : statusEnum.getName();
    }
}
