package com.fengrui.frmanage.common.enums;

/**
 * 业务错误码枚举。
 */
public enum BizErrorCode {

    PURCHASE_DEPT_NOT_FOUND(41001, "申请部门不存在"),
    PURCHASE_USER_NOT_FOUND(41002, "申请人不存在"),
    PURCHASE_USER_DISABLED(41003, "申请人已禁用"),
    PURCHASE_SUPPLIER_NOT_FOUND(41004, "供应商不存在"),
    PURCHASE_PRODUCT_NOT_FOUND(41005, "商品不存在或已禁用"),
    PURCHASE_NO_GENERATE_FAILED(41006, "采购单号生成失败"),
    PURCHASE_SAVE_FAILED(41007, "采购单保存失败，未获取到主键ID"),

    AUTH_FORBIDDEN(40301, "无权操作"),
    SUPPLIER_REFERENCED(42001, "供应商已被采购单引用，无法删除"),

    REDIS_UNAVAILABLE(50301, "Redis 服务不可用，请确认 localhost:6379 已启动"),
    DATABASE_ERROR(50302, "数据库操作失败");

    private final Integer code;

    private final String message;

    BizErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
