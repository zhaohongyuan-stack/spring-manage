package com.fengrui.frmanage;

import com.fengrui.frmanage.common.enums.ProductCategoryEnum;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.service.impl.dict.DictServiceImpl;
import com.fengrui.frmanage.vo.dict.RoleDictVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 字典业务服务测试。
 */
class DictServiceImplTest {

    private final DictServiceImpl dictService = new DictServiceImpl();

    @Test
    void listCategoriesShouldReturnAllPredefinedCategories() {
        List<String> categories = dictService.listCategories();

        assertEquals(ProductCategoryEnum.listAllNames(), categories);
        assertTrue(categories.contains("客房用品"));
        assertTrue(categories.contains("其他"));
    }

    @Test
    void listRolesShouldReturnAllRoleCodeAndName() {
        List<RoleDictVO> roles = dictService.listRoles();

        assertEquals(RoleEnum.values().length, roles.size());
        assertTrue(roles.stream().anyMatch(role -> "dept_head".equals(role.getCode()) && "部门负责人".equals(role.getName())));
        assertTrue(roles.stream().anyMatch(role -> "admin".equals(role.getCode()) && "系统管理员".equals(role.getName())));
    }
}
