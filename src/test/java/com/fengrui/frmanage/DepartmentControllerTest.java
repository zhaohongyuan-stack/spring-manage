package com.fengrui.frmanage;

import com.fengrui.frmanage.config.CorsConfig;
import com.fengrui.frmanage.config.SecurityConfig;
import com.fengrui.frmanage.controller.DepartmentController;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import com.fengrui.frmanage.service.department.DepartmentService;
import com.fengrui.frmanage.vo.DepartmentTreeVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 部门接口控制器测试。
 */
@WebMvcTest(DepartmentController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, CorsConfig.class})
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @Test
    void addDepartmentShouldReturnDepartmentIdWithoutAuthenticationInTemporaryPermitMode() throws Exception {
        when(departmentService.addDepartment(any())).thenReturn(5L);

        mockMvc.perform(post("/api/v1/department/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deptName": "会议宴会部",
                                  "parentId": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("新增成功"))
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    void addDepartmentShouldValidateRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/department/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deptName": "",
                                  "parentId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("部门名称不能为空")))
                .andExpect(jsonPath("$.message").value(containsString("上级部门ID不能为空")));
    }

    @Test
    void getDepartmentTreeShouldReturnTreeWithoutAuthentication() throws Exception {
        DepartmentTreeVO root = new DepartmentTreeVO();
        root.setId(1L);
        root.setDeptName("餐饮部");
        root.setParentId(0L);
        root.setChildren(new ArrayList<>());

        DepartmentTreeVO child = new DepartmentTreeVO();
        child.setId(5L);
        child.setDeptName("中餐厨房");
        child.setParentId(1L);
        child.setChildren(new ArrayList<>());
        root.getChildren().add(child);

        when(departmentService.getDepartmentTree()).thenReturn(List.of(root));

        mockMvc.perform(get("/api/v1/department/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].children[0].deptName").value("中餐厨房"));
    }
}
