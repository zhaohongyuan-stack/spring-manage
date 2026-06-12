package com.fengrui.frmanage.common.enums;

import java.util.Arrays;

/**
 * 系统角色枚举，统一维护 user.role 的角色编码和中文名称。
 */
public enum RoleEnum {

    ADMIN("admin", "系统管理员"),
    GM("gm", "酒店总经理"),
    DEPT_HEAD("dept_head", "部门负责人"),
    DEPT_EMPLOYEE("dept_employee", "部门员工"),
    PURCHASER("purchaser", "采购员"),
    WAREHOUSE_KEEPER("warehouse_keeper", "仓库管理员"),
    FINANCE("finance", "财务人员");

    private final String code;

    private final String name;

    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据角色编码获取枚举。
     *
     * @param code 角色编码
     * @return 角色枚举，不存在时返回 null
     */
    public static RoleEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(role -> role.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据角色编码获取中文名称。
     *
     * @param code 角色编码
     * @return 中文名称，不存在时返回原编码
     */
    public static String getNameByCode(String code) {
        RoleEnum roleEnum = getByCode(code);
        return roleEnum == null ? code : roleEnum.getName();
    }

    /**
     * 判断角色编码是否存在。
     *
     * @param code 角色编码
     * @return 是否存在
     */
    public static boolean containsCode(String code) {
        return getByCode(code) != null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
