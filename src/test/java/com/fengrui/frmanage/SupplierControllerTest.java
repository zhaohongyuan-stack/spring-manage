package com.fengrui.frmanage;

import com.fengrui.frmanage.config.SecurityConfig;
import com.fengrui.frmanage.controller.SupplierController;
import com.fengrui.frmanage.dto.supplier.SupplierQueryDTO;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import com.fengrui.frmanage.service.supplier.SupplierService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.supplier.SupplierVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 供应商接口控制器测试。
 */
@WebMvcTest(SupplierController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupplierService supplierService;

    @Test
    void addSupplierShouldReturnSupplierIdWithoutAuthenticationInTemporaryPermitMode() throws Exception {
        when(supplierService.addSupplier(any())).thenReturn(2L);

        mockMvc.perform(post("/api/v1/supplier/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierName": "大连XX食品配送中心",
                                  "contactPerson": "李主管",
                                  "contactPhone": "13900139000",
                                  "address": "大连市XX区XX路"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("新增成功"))
                .andExpect(jsonPath("$.data").value(2));
    }

    @Test
    void addSupplierShouldValidateRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/supplier/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierName": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("供应商名称不能为空")));
    }

    @Test
    void addSupplierShouldReturnBusinessErrorWhenNameDuplicated() throws Exception {
        when(supplierService.addSupplier(any())).thenThrow(new BusinessException("供应商名称已存在"));

        mockMvc.perform(post("/api/v1/supplier/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierName": "大连XX食品配送中心"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("供应商名称已存在"));
    }

    @Test
    void deleteSupplierShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/supplier/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteSupplierShouldReturnBusinessErrorWhenSupplierNotFound() throws Exception {
        doThrow(new BusinessException("供应商不存在")).when(supplierService).deleteSupplier(any());

        mockMvc.perform(post("/api/v1/supplier/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 999
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("供应商不存在"));
    }

    @Test
    void listSupplierShouldReturnPageResult() throws Exception {
        SupplierVO supplierVO = new SupplierVO();
        supplierVO.setSupplierId(1L);
        supplierVO.setSupplierName("沈阳XX酒店用品有限公司");
        supplierVO.setContactPerson("王经理");
        supplierVO.setContactPhone("13800138000");
        supplierVO.setAddress("沈阳市XX区");
        supplierVO.setStatus((short) 1);
        supplierVO.setCreateTime(LocalDateTime.of(2026, 6, 1, 9, 0));
        when(supplierService.listSupplier(any()))
                .thenReturn(new PageResultVO<>(1L, 1L, 10L, List.of(supplierVO)));

        mockMvc.perform(get("/api/v1/supplier/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list[0].supplierId").value(1))
                .andExpect(jsonPath("$.data.list[0].supplierName").value("沈阳XX酒店用品有限公司"));
    }

    @Test
    void listSupplierShouldBindSupplierNameQuery() throws Exception {
        when(supplierService.listSupplier(any()))
                .thenReturn(new PageResultVO<>(0L, 1L, 10L, List.of()));

        mockMvc.perform(get("/api/v1/supplier/list")
                        .param("pageNum", "2")
                        .param("pageSize", "5")
                        .param("supplierName", "食品"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<SupplierQueryDTO> queryCaptor = ArgumentCaptor.forClass(SupplierQueryDTO.class);
        verify(supplierService).listSupplier(queryCaptor.capture());
        assertEquals(2L, queryCaptor.getValue().getPageNum());
        assertEquals(5L, queryCaptor.getValue().getPageSize());
        assertEquals("食品", queryCaptor.getValue().getSupplierName());
    }
}
