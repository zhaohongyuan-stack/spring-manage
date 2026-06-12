package com.fengrui.frmanage.common.enums;

/**
 * 库存变动类型枚举。
 */
public enum InventoryChangeTypeEnum {

    NORMAL_INBOUND(1, "正常入库"),
    NORMAL_OUTBOUND(2, "正常出库"),
    DIRECT_FOOD(3, "食材直拨"),
    INITIAL_STOCK(4, "期初"),
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
}
