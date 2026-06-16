package com.fengrui.frmanage;

import com.fengrui.frmanage.config.SecurityConfig;
import com.fengrui.frmanage.controller.InventoryController;
import com.fengrui.frmanage.dto.inventory.InventoryRecordQueryDTO;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import com.fengrui.frmanage.service.inventory.InventoryService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.inventory.InventoryRecordVO;
import com.fengrui.frmanage.vo.inventory.InventoryVO;
import com.fengrui.frmanage.vo.inventory.InventoryWarningVO;
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
 * 库存接口控制器测试。
 */
@WebMvcTest(InventoryController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void listInventoryShouldReturnPageData() throws Exception {
        InventoryVO inventoryVO = new InventoryVO();
        inventoryVO.setProductId(1L);
        inventoryVO.setProductName("一次性牙具套装");
        inventoryVO.setStockQuantity(0);
        when(inventoryService.pageInventory(any()))
                .thenReturn(new PageResultVO<>(1L, 1L, 10L, List.of(inventoryVO)));

        mockMvc.perform(get("/api/v1/inventory/list")
                        .param("productName", "牙具")
                        .param("category", "客房用品"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].productId").value(1))
                .andExpect(jsonPath("$.data.list[0].stockQuantity").value(0));
    }

    @Test
    void warningShouldReturnSuggestQuantity() throws Exception {
        InventoryWarningVO warningVO = new InventoryWarningVO();
        warningVO.setProductId(1L);
        warningVO.setProductName("一次性牙具套装");
        warningVO.setStockQuantity(20);
        warningVO.setWarningThreshold(50);
        warningVO.setSuggestQuantity(80);
        when(inventoryService.pageWarning(any()))
                .thenReturn(new PageResultVO<>(1L, 1L, 10L, List.of(warningVO)));

        mockMvc.perform(get("/api/v1/inventory/warning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].suggestQuantity").value(80));
    }

    @Test
    void recordShouldBindQuery() throws Exception {
        when(inventoryService.pageRecord(any()))
                .thenReturn(new PageResultVO<>(0L, 2L, 5L, List.of()));

        mockMvc.perform(get("/api/v1/inventory/record")
                        .param("pageNum", "2")
                        .param("pageSize", "5")
                        .param("productId", "1")
                        .param("changeType", "2")
                        .param("relatedNo", "LY-202606")
                        .param("startTime", "2026-06-01T00:00:00")
                        .param("endTime", "2026-06-10T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<InventoryRecordQueryDTO> queryCaptor = ArgumentCaptor.forClass(InventoryRecordQueryDTO.class);
        verify(inventoryService).pageRecord(queryCaptor.capture());
        assertEquals(2L, queryCaptor.getValue().getPageNum());
        assertEquals(5L, queryCaptor.getValue().getPageSize());
        assertEquals(1L, queryCaptor.getValue().getProductId());
        assertEquals(2, queryCaptor.getValue().getChangeType());
        assertEquals("LY-202606", queryCaptor.getValue().getRelatedNo());
        assertEquals(LocalDateTime.of(2026, 6, 1, 0, 0), queryCaptor.getValue().getStartTime());
    }

    @Test
    void recordShouldReturnBusinessErrorForInvalidChangeType() throws Exception {
        doThrow(new BusinessException("库存变动类型不正确")).when(inventoryService).pageRecord(any());

        mockMvc.perform(get("/api/v1/inventory/record").param("changeType", "99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("库存变动类型不正确"));
    }

    @Test
    void initShouldValidateItems() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "operatorUserId": 1,
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("初始化明细不能为空")));
    }

    @Test
    void initShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "operatorUserId": 1,
                                  "items": [
                                    {"productId": 1, "initialQuantity": 100, "unitCost": 2.50}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("库存初始化完成"));
    }
}
