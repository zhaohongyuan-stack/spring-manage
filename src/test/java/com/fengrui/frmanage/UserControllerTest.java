package com.fengrui.frmanage;

import com.fengrui.frmanage.config.WebMvcBindingConfig;
import com.fengrui.frmanage.controller.UserController;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import com.fengrui.frmanage.service.UserService;
import com.fengrui.frmanage.vo.AddUserVO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.UserListVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户接口控制器测试。
 */
@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, WebMvcBindingConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void listUsersShouldWorkWithoutOptionalFilters() throws Exception {
        when(userService.listUsers(any())).thenReturn(new PageResultVO<>(0L, 1L, 10L, List.of()));

        mockMvc.perform(get("/api/v1/user/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("realName", "大张伟"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).listUsers(any());
    }

    @Test
    void listUsersShouldTreatEmptyDeptIdAsNotFiltered() throws Exception {
        when(userService.listUsers(any())).thenReturn(new PageResultVO<>(0L, 1L, 10L, List.of()));

        mockMvc.perform(get("/api/v1/user/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("deptId", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void addUserShouldReturnUserId() throws Exception {
        when(userService.addUser(any())).thenReturn(new AddUserVO(101L));

        mockMvc.perform(post("/api/v1/user/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "zhangwei",
                                  "password": "123456",
                                  "realName": "张伟",
                                  "role": "dept_head",
                                  "deptId": 1,
                                  "phone": "13812345678"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("新增成功"))
                .andExpect(jsonPath("$.data.userId").value(101));
    }

    @Test
    void addUserShouldAllowPurchaserWithoutDeptId() throws Exception {
        when(userService.addUser(any())).thenReturn(new AddUserVO(102L));

        mockMvc.perform(post("/api/v1/user/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "wang_procure",
                                  "password": "123456",
                                  "realName": "王采购",
                                  "role": "purchaser",
                                  "phone": "13812345679"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("新增成功"))
                .andExpect(jsonPath("$.data.userId").value(102));
    }

    @Test
    void addUserShouldValidateRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/user/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "password": "123456",
                                  "realName": "张伟",
                                  "role": "dept_head"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("登录账号不能为空"));
    }

    @Test
    void deleteUserShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 101
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteUserShouldReturnBusinessErrorWhenUserNotFound() throws Exception {
        doThrow(new BusinessException("用户不存在")).when(userService).deleteUser(any());

        mockMvc.perform(post("/api/v1/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 999
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    void deleteUserShouldValidateUserId() throws Exception {
        mockMvc.perform(post("/api/v1/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("用户ID不能为空")));
    }
}
