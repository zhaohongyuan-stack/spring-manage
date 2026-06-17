package com.fengrui.frmanage.common.util;

import com.fengrui.frmanage.common.enums.BizErrorCode;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.exception.BusinessException;

/**
 * 角色权限校验工具。
 */
public final class RoleAuthSupport {

    private RoleAuthSupport() {
    }

    /**
     * 判断用户是否具备指定角色。
     *
     * @param user 用户
     * @param role 角色
     * @return 是否具备指定角色
     */
    public static boolean hasRole(User user, RoleEnum role) {
        if (user == null || user.getRole() == null || role == null) {
            return false;
        }
        return role.getCode().equalsIgnoreCase(user.getRole().trim());
    }

    /**
     * 判断用户是否具备系统管理员或总经理特权。
     * 当前 admin 模式下，用于支持 username=admin 但 role=gm 的测试账号走全流程。
     *
     * @param user 用户
     * @return 是否具备超级管理权限
     */
    public static boolean isSuperManager(User user) {
        return hasRole(user, RoleEnum.ADMIN) || hasRole(user, RoleEnum.GM);
    }

    /**
     * 判断用户是否具备任一角色。
     *
     * @param user 用户
     * @param roles 允许角色
     * @return 是否具备任一角色
     */
    public static boolean hasAnyRole(User user, RoleEnum... roles) {
        if (isSuperManager(user)) {
            return true;
        }
        for (RoleEnum role : roles) {
            if (hasRole(user, role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验用户具备任一角色，否则抛出无权操作异常。
     *
     * @param user 用户
     * @param roles 允许角色
     */
    public static void assertAnyRole(User user, RoleEnum... roles) {
        if (hasAnyRole(user, roles)) {
            return;
        }
        throw forbidden(user);
    }

    /**
     * 构建无权操作异常，附带当前角色信息便于排查。
     *
     * @param user 用户
     * @return 业务异常
     */
    public static BusinessException forbidden(User user) {
        String roleCode = user == null || user.getRole() == null ? "未知" : user.getRole();
        String roleName = RoleEnum.getNameByCode(roleCode);
        return new BusinessException(
                BizErrorCode.AUTH_FORBIDDEN.getCode(),
                "无权操作，当前用户角色为：" + roleName + "(" + roleCode + ")"
        );
    }
}
