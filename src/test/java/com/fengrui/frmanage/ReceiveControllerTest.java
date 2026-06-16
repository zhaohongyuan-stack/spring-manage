package com.fengrui.frmanage;

import com.fengrui.frmanage.config.SecurityConfig;
import com.fengrui.frmanage.controller.ReceiveController;
import com.fengrui.frmanage.dto.receive.ReceiveListQueryDTO;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import com.fengrui.frmanage.service.receive.ReceiveService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.receive.ReceiveDetailVO;
import com.fengrui.frmanage.vo.receive.ReceiveListItemVO;
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
 * 领用出库接口控制器测试。
 */
@WebMvcTest(ReceiveController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ReceiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiveService receiveService;

    @Test
    void addReceiveShouldReturnReceiveIdAndNo() throws Exception {
        when(receiveService.addReceive(any()))
                .thenReturn(Map.of("receiveId", 201L, "receiveNo", "LY-20260616-001"));

        mockMvc.perform(post("/api/v1/receive/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deptId": 1,
                                  "applyUserId": 1001,
                                  "purpose": "客房用品补充",
                                  "remark": "急需",
                                  "items": [
                                    {"productId": 1, "quantity": 50}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("申请成功"))
                .andExpect(jsonPath("$.data.receiveId").value(201))
                .andExpect(jsonPath("$.data.receiveNo").value("LY-20260616-001"));
    }

    @Test
    void addReceiveShouldValidateItems() throws Exception {
        mockMvc.perform(post("/api/v1/receive/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deptId": 1,
                                  "applyUserId": 1001,
                                  "purpose": "客房用品补充",
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("领用明细不能为空")));
    }

    @Test
    void listReceiveShouldBindQuery() throws Exception {
        when(receiveService.pageList(any()))
                .thenReturn(new PageResultVO<>(0L, 2L, 5L, List.of()));

        mockMvc.perform(get("/api/v1/receive/list")
                        .param("pageNum", "2")
                        .param("pageSize", "5")
                        .param("status", "1")
                        .param("deptId", "3")
                        .param("applyUserId", "1001")
                        .param("startTime", "2026-06-01T00:00:00")
                        .param("endTime", "2026-06-10T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<ReceiveListQueryDTO> queryCaptor = ArgumentCaptor.forClass(ReceiveListQueryDTO.class);
        verify(receiveService).pageList(queryCaptor.capture());
        assertEquals(2L, queryCaptor.getValue().getPageNum());
        assertEquals(5L, queryCaptor.getValue().getPageSize());
        assertEquals(1, queryCaptor.getValue().getStatus());
        assertEquals(3L, queryCaptor.getValue().getDeptId());
        assertEquals(1001L, queryCaptor.getValue().getApplyUserId());
        assertEquals(LocalDateTime.of(2026, 6, 1, 0, 0), queryCaptor.getValue().getStartTime());
    }

    @Test
    void detailShouldValidateReceiveId() throws Exception {
        mockMvc.perform(get("/api/v1/receive/detail"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("领用单ID不能为空")));
    }

    @Test
    void detailShouldReturnReceiveDetail() throws Exception {
        ReceiveDetailVO detailVO = new ReceiveDetailVO();
        detailVO.setReceiveId(201L);
        detailVO.setReceiveNo("LY-20260616-001");
        when(receiveService.detail(201L)).thenReturn(detailVO);

        mockMvc.perform(get("/api/v1/receive/detail").param("receiveId", "201"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receiveId").value(201))
                .andExpect(jsonPath("$.data.receiveNo").value("LY-20260616-001"));
    }

    @Test
    void approveShouldReturnBusinessError() throws Exception {
        doThrow(new BusinessException("驳回时必须填写驳回理由")).when(receiveService).approve(any());

        mockMvc.perform(post("/api/v1/receive/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiveId": 201,
                                  "approverUserId": 1005,
                                  "approved": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("驳回时必须填写驳回理由"));
    }

    @Test
    void confirmShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/receive/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiveId": 201,
                                  "delivererUserId": 1010,
                                  "remark": "已全部发货"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("出库成功"));
    }

    @Test
    void cancelShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/receive/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiveId": 201,
                                  "operatorUserId": 1001
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("已取消"));
    }
}
