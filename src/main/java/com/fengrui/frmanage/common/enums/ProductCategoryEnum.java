package com.fengrui.frmanage.common.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 商品分类枚举，统一维护 product.category 的预定义分类。
 */
public enum ProductCategoryEnum {

    ROOM_SUPPLIES("客房用品"),
    FOOD("食材"),
    ENGINEERING("工程物资"),
    OFFICE("办公用品"),
    CLEANING("清洁用品"),
    OTHER("其他");

    private final String name;

    ProductCategoryEnum(String name) {
        this.name = name;
    }

    /**
     * 获取分类中文名称。
     *
     * @return 分类名称
     */
    public String getName() {
        return name;
    }

    /**
     * 返回所有商品分类名称，供字典接口与前端下拉使用。
     *
     * @return 分类名称列表
     */
    public static List<String> listAllNames() {
        return Arrays.stream(values())
                .map(ProductCategoryEnum::getName)
                .toList();
    }

    /**
     * 判断分类名称是否在预定义范围内。
     *
     * @param category 分类名称
     * @return 是否有效
     */
    public static boolean containsName(String category) {
        return Arrays.stream(values())
                .anyMatch(item -> item.name.equals(category));
    }
}
