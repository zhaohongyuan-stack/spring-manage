package com.fengrui.frmanage.common.enums;

import java.util.Arrays;

/**
 * 库存变动类型枚举。
 */
public enum InventoryChangeTypeEnum {

    NORMAL_INBOUND(1, "正常入库"),
    NORMAL_OUTBOUND(2, "正常出库"),
    DIRECT_FOOD(3, "食材直拨"),
    INITIAL_STOCK(4, "期初初始化"),
    STOCKTAKING_ADJUSTMENT(5, "盘点");

    private final Integer code;

    private final String name;

    InventoryChangeTypeEnum(Integer code, String name) {
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
     * 获取库存变动类型描述。
     *
     * @return 类型描述
     */
    public String getDescription() {
        return name;
    }

    /**
     * 根据编码获取库存变动类型。
     *
     * @param code 类型编码
     * @return 库存变动类型，不存在时返回 null
     */
    public static InventoryChangeTypeEnum getByCode(Integer code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据编码获取库存变动类型描述。
     *
     * @param code 类型编码
     * @return 类型描述，不存在时返回原编码文本
     */
    public static String getDescriptionByCode(Integer code) {
        InventoryChangeTypeEnum typeEnum = getByCode(code);
        return typeEnum == null ? String.valueOf(code) : typeEnum.getDescription();
    }
}
