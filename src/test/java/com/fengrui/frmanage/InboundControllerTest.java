package com.fengrui.frmanage;

import com.fengrui.frmanage.config.SecurityConfig;
import com.fengrui.frmanage.controller.InboundController;
import com.fengrui.frmanage.dto.inbound.InboundListQueryDTO;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import com.fengrui.frmanage.service.inbound.InboundService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.inbound.InboundDetailVO;
import com.fengrui.frmanage.vo.inbound.InboundListItemVO;
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
 * 入库接口控制器测试。
 */
@WebMvcTest(InboundController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class InboundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InboundService inboundService;

    @Test
    void addInboundShouldReturnInboundIdAndNo() throws Exception {
        when(inboundService.addInbound(any()))
                .thenReturn(Map.of("inboundId", 301L, "inboundNo", "RK-20260616-001"));

        mockMvc.perform(post("/api/v1/inbound/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "purchaseId": 101,
                                  "receiverUserId": 2001,
                                  "inboundType": 1,
                                  "remark": "验收前入库",
                                  "items": [
                                    {"productId": 1, "actualQuantity": 98, "batchNo": "B01", "expiryDate": "2027-06-01"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("入库单创建成功，等待验收"))
                .andExpect(jsonPath("$.data.inboundId").value(301))
                .andExpect(jsonPath("$.data.inboundNo").value("RK-20260616-001"));
    }

    @Test
    void addInboundShouldValidateItems() throws Exception {
        mockMvc.perform(post("/api/v1/inbound/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "purchaseId": 101,
                                  "receiverUserId": 2001,
                                  "inboundType": 1,
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("入库明细不能为空")));
    }

    @Test
    void listInboundShouldBindQuery() throws Exception {
        when(inboundService.pageList(any())).thenReturn(new PageResultVO<>(0L, 2L, 5L, List.of()));

        mockMvc.perform(get("/api/v1/inbound/list")
                        .param("pageNum", "2")
                        .param("pageSize", "5")
                        .param("inboundType", "1")
                        .param("status", "1")
                        .param("purchaseNo", "CG-202606"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<InboundListQueryDTO> queryCaptor = ArgumentCaptor.forClass(InboundListQueryDTO.class);
        verify(inboundService).pageList(queryCaptor.capture());
        assertEquals(2L, queryCaptor.getValue().getPageNum());
        assertEquals(5L, queryCaptor.getValue().getPageSize());
        assertEquals(1, queryCaptor.getValue().getInboundType());
        assertEquals(1, queryCaptor.getValue().getStatus());
        assertEquals("CG-202606", queryCaptor.getValue().getPurchaseNo());
    }

    @Test
    void detailShouldValidateInboundId() throws Exception {
        mockMvc.perform(get("/api/v1/inbound/detail"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("入库单ID不能为空")));
    }

    @Test
    void detailShouldReturnInboundDetail() throws Exception {
        InboundDetailVO detailVO = new InboundDetailVO();
        detailVO.setInboundId(301L);
        detailVO.setInboundNo("RK-20260616-001");
        when(inboundService.detail(301L)).thenReturn(detailVO);

        mockMvc.perform(get("/api/v1/inbound/detail").param("inboundId", "301"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inboundId").value(301))
                .andExpect(jsonPath("$.data.inboundNo").value("RK-20260616-001"));
    }

    @Test
    void confirmShouldReturnBusinessError() throws Exception {
        doThrow(new BusinessException("验收人必须是申请部门负责人")).when(inboundService).confirm(any());

        mockMvc.perform(post("/api/v1/inbound/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inboundId": 301,
                                  "deptHeadUserId": 1005,
                                  "warehouseKeeperUserId": 2001
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("验收人必须是申请部门负责人"));
    }
}
