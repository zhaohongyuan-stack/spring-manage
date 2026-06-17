package com.fengrui.frmanage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fengrui.frmanage.common.enums.InboundStatusEnum;
import com.fengrui.frmanage.common.enums.InboundTypeEnum;
import com.fengrui.frmanage.common.enums.PurchaseStatusEnum;
import com.fengrui.frmanage.common.enums.ReceiveStatusEnum;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.entity.Inventory;
import com.fengrui.frmanage.entity.Product;
import com.fengrui.frmanage.entity.Supplier;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.mapper.InventoryMapper;
import com.fengrui.frmanage.mapper.ProductMapper;
import com.fengrui.frmanage.mapper.SupplierMapper;
import com.fengrui.frmanage.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 采购到领用快乐路径链路集成测试。
 * 依赖本地 PostgreSQL 与 Redis，按接口顺序验证状态流转并输出关键字段日志。
 *
 * <p>运行方式：</p>
 * <pre>mvnw test -Dtest=PurchaseToReceiveHappyPathIntegrationTest</pre>
 *
 * <p>前置数据（需提前写入数据库）：</p>
 * <ul>
 *   <li>用户 100：dept_employee，dept_id=1</li>
 *   <li>用户 101：dept_head，dept_id=1</li>
 *   <li>用户 102：purchaser</li>
 *   <li>至少一名 warehouse_keeper 用户（入库/出库经办人，自动解析）</li>
 *   <li>商品 301：status=1（启用）</li>
 *   <li>至少一条供应商记录</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Tag("integration")
class PurchaseToReceiveHappyPathIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PurchaseToReceiveHappyPathIntegrationTest.class);

    private static final Long DEPT_ID = 1L;
    private static final Long APPLY_USER_ID = 100L;
    private static final Long DEPT_HEAD_USER_ID = 101L;
    private static final Long PURCHASER_USER_ID = 102L;
    private static final Long PRODUCT_ID = 301L;
    private static final int PURCHASE_QTY = 100;
    private static final int RECEIVE_QTY = 20;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    private Long warehouseKeeperUserId;
    private Long supplierId;
    private BigDecimal unitPrice;
    private int initialStock;

  /**
   * 校验链路测试前置数据，并解析仓库管理员与供应商。
   */
    @BeforeEach
    void setUpPrerequisites() {
        StringBuilder missing = new StringBuilder();
        if (userMapper.selectById(APPLY_USER_ID) == null) {
            missing.append("用户").append(APPLY_USER_ID).append("(申请人) ");
        }
        if (userMapper.selectById(DEPT_HEAD_USER_ID) == null) {
            missing.append("用户").append(DEPT_HEAD_USER_ID).append("(部门负责人) ");
        }
        if (userMapper.selectById(PURCHASER_USER_ID) == null) {
            missing.append("用户").append(PURCHASER_USER_ID).append("(采购员) ");
        }
        if (productMapper.selectById(PRODUCT_ID) == null) {
            missing.append("商品").append(PRODUCT_ID).append(" ");
        }
        if (supplierMapper.selectOne(new LambdaQueryWrapper<Supplier>().last("LIMIT 1")) == null) {
            missing.append("供应商 ");
        }
        User warehouseKeeper = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getRole, RoleEnum.WAREHOUSE_KEEPER.getCode())
                .eq(User::getStatus, 1)
                .last("LIMIT 1"));
        if (warehouseKeeper == null) {
            missing.append("warehouse_keeper用户 ");
        }
        assumeTrue(missing.isEmpty(),
                "链路测试前置数据缺失，请补齐后重试: " + missing);

        Product product = productMapper.selectById(PRODUCT_ID);
        unitPrice = product.getCostPrice() != null && product.getCostPrice().compareTo(BigDecimal.ZERO) > 0
                ? product.getCostPrice() : new BigDecimal("2.20");

        Supplier supplier = supplierMapper.selectOne(new LambdaQueryWrapper<Supplier>().last("LIMIT 1"));
        supplierId = supplier.getId();

        warehouseKeeperUserId = warehouseKeeper.getId();

        Inventory inventory = inventoryMapper.selectById(PRODUCT_ID);
        initialStock = inventory == null ? 0 : inventory.getStockQuantity();

        logUserRole("申请人", APPLY_USER_ID);
        logUserRole("部门负责人", DEPT_HEAD_USER_ID);
        logUserRole("采购员", PURCHASER_USER_ID);
        logUserRole("仓库管理员", warehouseKeeperUserId);
        logBanner("链路测试前置检查通过");
        logInventorySnapshot("测试开始前库存快照");
    }

    /**
     * 快乐路径：采购申请 → 审批 → 采购确认 → 入库 → 验收 → 领用 → 审批 → 出库。
     */
    @Test
    @DisplayName("采购到领用快乐路径链路测试")
    void happyPathFromPurchaseToReceive() throws Exception {
        Long purchaseId;
        Long inboundId;
        Long receiveId;

        // 1. 新增采购单
        logStep(1, "新增采购单", "applyUserId=" + APPLY_USER_ID + ", productId=" + PRODUCT_ID + ", qty=" + PURCHASE_QTY);
        JsonNode addPurchaseResp = postJson("/api/v1/purchase/add", """
                {
                  "deptId": %d,
                  "applyUserId": %d,
                  "requireDate": "%s",
                  "supplierId": %d,
                  "remark": "链路测试-采购申请",
                  "items": [
                    {"productId": %d, "quantity": %d, "unitPrice": %s}
                  ]
                }
                """.formatted(DEPT_ID, APPLY_USER_ID, LocalDate.now(), supplierId, PRODUCT_ID, PURCHASE_QTY, unitPrice));
        purchaseId = addPurchaseResp.path("data").path("purchaseId").asLong();
        String purchaseNo = addPurchaseResp.path("data").path("purchaseNo").asText();
        assertTrue(purchaseNo.startsWith("CG-"), "采购单号应以 CG- 开头");
        logPurchaseSnapshot(purchaseId, "步骤1完成后");

        // 2. 审批采购
        logStep(2, "审批采购", "purchaseId=" + purchaseId + ", approverUserId=" + DEPT_HEAD_USER_ID + ", approved=true");
        postJson("/api/v1/purchase/approve", """
                {
                  "purchaseId": %d,
                  "approverUserId": %d,
                  "approved": true
                }
                """.formatted(purchaseId, DEPT_HEAD_USER_ID));
        logPurchaseSnapshot(purchaseId, "步骤2完成后");
        assertPurchaseStatus(purchaseId, PurchaseStatusEnum.APPROVED);

        // 3. 采购确认
        logStep(3, "采购确认", "purchaseId=" + purchaseId + ", purchaserUserId=" + PURCHASER_USER_ID);
        postJson("/api/v1/purchase/confirm", """
                {
                  "purchaseId": %d,
                  "purchaserUserId": %d
                }
                """.formatted(purchaseId, PURCHASER_USER_ID));
        logPurchaseSnapshot(purchaseId, "步骤3完成后");
        assertPurchaseStatus(purchaseId, PurchaseStatusEnum.PURCHASING);

        // 4. 创建入库单（足量）
        logStep(4, "创建入库单", "purchaseId=" + purchaseId + ", actualQuantity=" + PURCHASE_QTY);
        JsonNode addInboundResp = postJson("/api/v1/inbound/add", """
                {
                  "purchaseId": %d,
                  "receiverUserId": %d,
                  "inboundType": %d,
                  "remark": "链路测试-入库",
                  "items": [
                    {"productId": %d, "actualQuantity": %d}
                  ]
                }
                """.formatted(purchaseId, warehouseKeeperUserId, InboundTypeEnum.NORMAL.getCode(),
                PRODUCT_ID, PURCHASE_QTY));
        inboundId = addInboundResp.path("data").path("inboundId").asLong();
        String inboundNo = addInboundResp.path("data").path("inboundNo").asText();
        assertTrue(inboundNo.startsWith("RK-"), "入库单号应以 RK- 开头");
        logInboundSnapshot(inboundId, "步骤4完成后");
        assertInboundStatus(inboundId, InboundStatusEnum.PENDING_ACCEPTANCE);
        assertInventoryQuantity(initialStock, "创建入库单后库存应未变化");

        // 5. 验收确认
        logStep(5, "验收确认", "inboundId=" + inboundId + ", deptHeadUserId=" + DEPT_HEAD_USER_ID);
        postJson("/api/v1/inbound/confirm", """
                {
                  "inboundId": %d,
                  "deptHeadUserId": %d,
                  "warehouseKeeperUserId": %d
                }
                """.formatted(inboundId, DEPT_HEAD_USER_ID, warehouseKeeperUserId));
        logInboundSnapshot(inboundId, "步骤5完成后");
        logPurchaseSnapshot(purchaseId, "步骤5完成后-关联采购单");
        assertInboundStatus(inboundId, InboundStatusEnum.ACCEPTED);
        assertPurchaseStatus(purchaseId, PurchaseStatusEnum.INBOUNDED);
        assertInventoryQuantity(initialStock + PURCHASE_QTY, "验收后库存应增加 " + PURCHASE_QTY);

        // 6. 新增领用单
        logStep(6, "新增领用单", "deptId=" + DEPT_ID + ", applyUserId=" + APPLY_USER_ID + ", qty=" + RECEIVE_QTY);
        JsonNode addReceiveResp = postJson("/api/v1/receive/add", """
                {
                  "deptId": %d,
                  "applyUserId": %d,
                  "purpose": "链路测试-客房用品领用",
                  "remark": "快乐路径领用",
                  "items": [
                    {"productId": %d, "quantity": %d}
                  ]
                }
                """.formatted(DEPT_ID, APPLY_USER_ID, PRODUCT_ID, RECEIVE_QTY));
        receiveId = addReceiveResp.path("data").path("receiveId").asLong();
        String receiveNo = addReceiveResp.path("data").path("receiveNo").asText();
        assertTrue(receiveNo.startsWith("LY-"), "领用单号应以 LY- 开头");
        logReceiveSnapshot(receiveId, "步骤6完成后");
        assertReceiveStatus(receiveId, ReceiveStatusEnum.PENDING_APPROVAL);

        // 7. 审批领用
        logStep(7, "审批领用", "receiveId=" + receiveId + ", approverUserId=" + DEPT_HEAD_USER_ID + ", approved=true");
        postJson("/api/v1/receive/approve", """
                {
                  "receiveId": %d,
                  "approverUserId": %d,
                  "approved": true
                }
                """.formatted(receiveId, DEPT_HEAD_USER_ID));
        logReceiveSnapshot(receiveId, "步骤7完成后");
        assertReceiveStatus(receiveId, ReceiveStatusEnum.PENDING_OUTBOUND);

        // 8. 确认出库
        logStep(8, "确认出库", "receiveId=" + receiveId + ", delivererUserId=" + warehouseKeeperUserId);
        postJson("/api/v1/receive/confirm", """
                {
                  "receiveId": %d,
                  "delivererUserId": %d,
                  "remark": "链路测试出库完成"
                }
                """.formatted(receiveId, warehouseKeeperUserId));
        logReceiveSnapshot(receiveId, "步骤8完成后");
        logInventorySnapshot("步骤8完成后库存快照");
        assertReceiveStatus(receiveId, ReceiveStatusEnum.OUTBOUNDED);
        assertInventoryQuantity(initialStock + PURCHASE_QTY - RECEIVE_QTY,
                "出库后库存应为初始+" + (PURCHASE_QTY - RECEIVE_QTY));

        logBanner("快乐路径链路测试全部通过");
        log.info("采购单: {} (id={})", purchaseNo, purchaseId);
        log.info("入库单: {} (id={})", inboundNo, inboundId);
        log.info("领用单: {} (id={})", receiveNo, receiveId);
    }

    /**
     * 发送 POST JSON 请求并校验业务成功。
     *
     * @param url 接口地址
     * @param body 请求体
     * @return 响应 data 节点
     */
    private JsonNode postJson(String url, String body) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        log.info(">>> POST {} => code={}, message={}", url, root.path("code").asInt(), root.path("message").asText());
        return root;
    }

    /**
     * 查询采购单详情并输出关键字段。
     *
     * @param purchaseId 采购单ID
     * @param label 日志标签
     */
    private void logPurchaseSnapshot(Long purchaseId, String label) throws Exception {
        JsonNode detail = getDetail("/api/v1/purchase/detail", "purchaseId", purchaseId);
        String message = String.format(
                "[采购单-%s] id=%s, no=%s, status=%s(%s), purchaserId=%s, receivedQuantity=%s",
                label,
                detail.path("purchaseId").asText(),
                detail.path("purchaseNo").asText(),
                detail.path("status").asText(),
                detail.path("statusName").asText(),
                detail.path("purchaserId").isNull() ? "null" : detail.path("purchaserId").asText(),
                detail.path("items").isArray() && !detail.path("items").isEmpty()
                        ? detail.path("items").get(0).path("receivedQuantity").asText() : "N/A");
        logSnapshot(message);
    }

    /**
     * 查询入库单详情并输出关键字段。
     *
     * @param inboundId 入库单ID
     * @param label 日志标签
     */
    private void logInboundSnapshot(Long inboundId, String label) throws Exception {
        JsonNode detail = getDetail("/api/v1/inbound/detail", "inboundId", inboundId);
        String message = String.format(
                "[入库单-%s] id=%s, no=%s, status=%s(%s), purchaseId=%s, deptHeadId=%s, acceptTime=%s",
                label,
                detail.path("inboundId").asText(),
                detail.path("inboundNo").asText(),
                detail.path("status").asText(),
                detail.path("statusName").asText(),
                detail.path("purchaseId").asText(),
                detail.path("deptHeadId").isNull() ? "null" : detail.path("deptHeadId").asText(),
                detail.path("acceptTime").isNull() ? "null" : detail.path("acceptTime").asText());
        logSnapshot(message);
    }

    /**
     * 查询领用单详情并输出关键字段。
     *
     * @param receiveId 领用单ID
     * @param label 日志标签
     */
    private void logReceiveSnapshot(Long receiveId, String label) throws Exception {
        JsonNode detail = getDetail("/api/v1/receive/detail", "receiveId", receiveId);
        String message = String.format(
                "[领用单-%s] id=%s, no=%s, status=%s(%s), delivererId=%s, deliverTime=%s",
                label,
                detail.path("receiveId").asText(),
                detail.path("receiveNo").asText(),
                detail.path("status").asText(),
                detail.path("statusName").asText(),
                detail.path("delivererId").isNull() ? "null" : detail.path("delivererId").asText(),
                detail.path("deliverTime").isNull() ? "null" : detail.path("deliverTime").asText());
        logSnapshot(message);
    }

    /**
     * 输出库存快照日志。
     *
     * @param label 日志标签
     */
    private void logInventorySnapshot(String label) {
        Inventory inventory = inventoryMapper.selectById(PRODUCT_ID);
        String message = inventory == null
                ? String.format("[库存-%s] productId=%s, stockQuantity=0 (无库存记录)", label, PRODUCT_ID)
                : String.format("[库存-%s] productId=%s, stockQuantity=%s, unitCost=%s, totalCost=%s",
                label, PRODUCT_ID, inventory.getStockQuantity(), inventory.getUnitCost(), inventory.getTotalCost());
        logSnapshot(message);
    }

    /**
     * 调用详情接口获取 data 节点。
     *
     * @param url 接口地址
     * @param paramName 查询参数名
     * @param id 业务ID
     * @return 详情 data
     */
    private JsonNode getDetail(String url, String paramName, Long id) throws Exception {
        MvcResult result = mockMvc.perform(get(url).param(paramName, String.valueOf(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private void assertPurchaseStatus(Long purchaseId, PurchaseStatusEnum expected) throws Exception {
        JsonNode detail = getDetail("/api/v1/purchase/detail", "purchaseId", purchaseId);
        assertEquals(expected.getCode(), detail.path("status").asInt());
    }

    private void assertInboundStatus(Long inboundId, InboundStatusEnum expected) throws Exception {
        JsonNode detail = getDetail("/api/v1/inbound/detail", "inboundId", inboundId);
        assertEquals(expected.getCode(), detail.path("status").asInt());
    }

    private void assertReceiveStatus(Long receiveId, ReceiveStatusEnum expected) throws Exception {
        JsonNode detail = getDetail("/api/v1/receive/detail", "receiveId", receiveId);
        assertEquals(expected.getCode(), detail.path("status").asInt());
    }

    private void assertInventoryQuantity(int expected, String message) {
        Inventory inventory = inventoryMapper.selectById(PRODUCT_ID);
        int actual = inventory == null ? 0 : inventory.getStockQuantity();
        assertEquals(expected, actual, message);
    }

    private void logStep(int step, String action, String params) {
        logBanner("步骤" + step + ": " + action + " | " + params);
    }

    private void logBanner(String message) {
        String line = "=".repeat(72);
        String output = "\n" + line + "\n" + message + "\n" + line;
        System.out.println(output);
        log.info(output);
    }

    private void logSnapshot(String message) {
        System.out.println(message);
        log.info(message);
    }

    private void logUserRole(String label, Long userId) {
        User user = userMapper.selectById(userId);
        assertNotNull(user, label + " 用户不存在: " + userId);
        String message = String.format("[用户] %s id=%s, username=%s, role=%s, deptId=%s",
                label, user.getId(), user.getUsername(), user.getRole(), user.getDeptId());
        logSnapshot(message);
    }
}
