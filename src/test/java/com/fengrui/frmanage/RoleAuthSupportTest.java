package com.fengrui.frmanage;

import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.common.util.RoleAuthSupport;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 角色权限工具测试。
 */
class RoleAuthSupportTest {

    @Test
    void isSuperManagerShouldAcceptAdminAndGm() {
        assertTrue(RoleAuthSupport.isSuperManager(buildUser("admin")));
        assertTrue(RoleAuthSupport.isSuperManager(buildUser("gm")));
        assertFalse(RoleAuthSupport.isSuperManager(buildUser("purchaser")));
    }

    @Test
    void assertAnyRoleShouldAllowGmForPurchaserAction() {
        RoleAuthSupport.assertAnyRole(buildUser("gm"), RoleEnum.PURCHASER, RoleEnum.ADMIN);
    }

    @Test
    void forbiddenShouldIncludeCurrentRole() {
        BusinessException exception = RoleAuthSupport.forbidden(buildUser("dept_head"));
        assertEquals("无权操作，当前用户角色为：部门负责人(dept_head)", exception.getMessage());
    }

    private User buildUser(String role) {
        User user = new User();
        user.setId(1L);
        user.setRole(role);
        return user;
    }
}
