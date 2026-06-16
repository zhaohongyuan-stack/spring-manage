package com.fengrui.frmanage;

import com.fengrui.frmanage.config.SecurityConfig;
import com.fengrui.frmanage.controller.PurchaseController;
import com.fengrui.frmanage.dto.purchase.PurchaseListQueryDTO;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import com.fengrui.frmanage.service.purchase.PurchaseService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.purchase.PurchaseDetailVO;
import com.fengrui.frmanage.vo.purchase.PurchaseListItemVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

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
 * 采购接口控制器测试。
 */
@WebMvcTest(PurchaseController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseService purchaseService;

    @Test
    void addPurchaseShouldReturnPurchaseIdAndNo() throws Exception {
        when(purchaseService.addPurchase(any()))
                .thenReturn(Map.of("purchaseId", 101L, "purchaseNo", "CG-20260616-001"));

        mockMvc.perform(post("/api/v1/purchase/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deptId": 1,
                                  "applyUserId": 1001,
                                  "requireDate": "2099-06-20",
                                  "supplierId": 3,
                                  "remark": "客房用品补充",
                                  "items": [
                                    {"productId": 1, "quantity": 100, "unitPrice": 2.20}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("申请成功"))
                .andExpect(jsonPath("$.data.purchaseId").value(101))
                .andExpect(jsonPath("$.data.purchaseNo").value("CG-20260616-001"));
    }

    @Test
    void addPurchaseShouldValidateItems() throws Exception {
        mockMvc.perform(post("/api/v1/purchase/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deptId": 1,
                                  "applyUserId": 1001,
                                  "requireDate": "2099-06-20",
                                  "supplierId": 3,
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("采购明细不能为空")));
    }

    @Test
    void listPurchaseShouldBindQuery() throws Exception {
        when(purchaseService.pageList(any()))
                .thenReturn(new PageResultVO<>(0L, 2L, 5L, List.of()));

        mockMvc.perform(get("/api/v1/purchase/list")
                        .param("pageNum", "2")
                        .param("pageSize", "5")
                        .param("operatorUserId", "1")
                        .param("status", "1")
                        .param("purchaseNo", "CG-20260616"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<PurchaseListQueryDTO> queryCaptor = ArgumentCaptor.forClass(PurchaseListQueryDTO.class);
        verify(purchaseService).pageList(queryCaptor.capture());
        assertEquals(2L, queryCaptor.getValue().getPageNum());
        assertEquals(5L, queryCaptor.getValue().getPageSize());
        assertEquals(1L, queryCaptor.getValue().getOperatorUserId());
        assertEquals(1, queryCaptor.getValue().getStatus());
        assertEquals("CG-20260616", queryCaptor.getValue().getPurchaseNo());
    }

    @Test
    void detailShouldValidatePurchaseId() throws Exception {
        mockMvc.perform(get("/api/v1/purchase/detail"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("采购单ID不能为空")));
    }

    @Test
    void detailShouldReturnPurchaseDetail() throws Exception {
        PurchaseDetailVO detailVO = new PurchaseDetailVO();
        detailVO.setPurchaseId(101L);
        detailVO.setPurchaseNo("CG-20260616-001");
        when(purchaseService.detail(101L)).thenReturn(detailVO);

        mockMvc.perform(get("/api/v1/purchase/detail").param("purchaseId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purchaseId").value(101))
                .andExpect(jsonPath("$.data.purchaseNo").value("CG-20260616-001"));
    }

    @Test
    void approveShouldReturnBusinessError() throws Exception {
        doThrow(new BusinessException("只有待审批采购单可以审批")).when(purchaseService).approve(any());

        mockMvc.perform(post("/api/v1/purchase/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "purchaseId": 101,
                                  "approverUserId": 1005,
                                  "approved": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("只有待审批采购单可以审批"));
    }

    @Test
    void confirmShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/purchase/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "purchaseId": 101,
                                  "purchaserUserId": 1008
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("已确认为采购中"));
    }

    @Test
    void cancelShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/purchase/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "purchaseId": 101,
                                  "operatorUserId": 1001
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("已取消"));
    }
}
