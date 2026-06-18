package com.fengrui.frmanage;

import com.fengrui.frmanage.config.CorsConfig;
import com.fengrui.frmanage.config.SecurityConfig;
import com.fengrui.frmanage.controller.DictController;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import com.fengrui.frmanage.service.dict.DictService;
import com.fengrui.frmanage.vo.dict.RoleDictVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 字典接口控制器测试。
 */
@WebMvcTest(DictController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, CorsConfig.class})
class DictControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DictService dictService;

    @Test
    void listCategoriesShouldReturnCategoryNames() throws Exception {
        when(dictService.listCategories()).thenReturn(List.of("客房用品", "食材", "其他"));

        mockMvc.perform(get("/api/v1/dict/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("客房用品"))
                .andExpect(jsonPath("$.data[2]").value("其他"));
    }

    @Test
    void listRolesShouldReturnRoleCodeAndName() throws Exception {
        when(dictService.listRoles()).thenReturn(List.of(
                new RoleDictVO("dept_employee", "部门员工"),
                new RoleDictVO("dept_head", "部门负责人")
        ));

        mockMvc.perform(get("/api/v1/dict/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].code").value("dept_employee"))
                .andExpect(jsonPath("$.data[0].name").value("部门员工"))
                .andExpect(jsonPath("$.data[1].code").value("dept_head"))
                .andExpect(jsonPath("$.data[1].name").value("部门负责人"));
    }
}
