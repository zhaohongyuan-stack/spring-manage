# 酒店进销存管理系统（FrManage）项目介绍

# 1 项目背景

酒店日常运营涉及客房、餐饮、工程等多部门物资的采购、入库、库存与领用。传统方式常见痛点包括：

- **单据分散**：采购申请、入库验收、领用出库各自记录，难以追溯完整链路。
- **状态不清**：采购单走到哪一步、库存是否充足、谁审批谁验收，缺少统一状态机。
- **权限混乱**：跨部门审批、采购员确认、仓管出入库若缺少角色约束，易产生越权操作。
- **成本难算**：入库后需按移动加权平均维护成本单价，出库时需快照单价并写库存流水。

本项目 **FrManage** 是一套基于 **Spring Boot 3** 的后端服务，面向酒店进销存场景，提供从 **采购申请 → 审批 → 采购确认 → 入库 → 验收 → 领用 → 出库** 的完整 REST API，并配套 Swagger 文档、单元/集成测试与部署说明。

# 2 项目介绍

本项目通过分层 REST 接口与数据库事务，串联酒店物资进销存主流程，支持多角色协同。

- **基础信息统一管理**：用户、部门树、供应商、商品主数据；商品支持逻辑禁用与「名称+规格」唯一校验。
- **采购全流程**：员工申请 → 部门负责人审批 → 采购员确认 → 生成采购单号 `CG-yyyyMMdd-xxx`。
- **入库与验收**：仓管按采购单创建入库单（`RK-...`），部门负责人验收后更新库存与采购单状态。
- **领用出库**：部门员工申请 → 负责人审批 → 仓管确认出库，扣减库存并写流水（`LY-...`）。
- **角色与权限**：业务层通过 `RoleAuthSupport` 校验审批人、采购员、仓管等角色；`admin` / `gm` 具备全流程超级权限（联调阶段）。
- **可观测与联调**：Swagger UI、统一 `Result` 响应、业务错误码（如 `40301` 无权操作、`50301` Redis 不可用）；提供采购到领用快乐路径集成测试。

**技术栈**：Java 17、Spring Boot 3.2.12、MyBatis-Plus 3.5.7、PostgreSQL、Redis（单号递增）、SpringDoc OpenAPI。

# 3 项目实现

## 3.1 核心组件

### 3.1.1 分层架构

```text
Controller（REST + 参数校验）
    ↓
Service（业务规则 + @Transactional）
    ↓
Mapper（MyBatis-Plus）
    ↓
PostgreSQL（12 张业务表） + Redis（单号）
```

统一响应封装：

```java
public class Result<T> {
    private Integer code;    // 200 成功，400 参数/业务错误，503xx 基础设施
    private String message;
    private T data;
}
```

### 3.1.2 Redis 业务单号服务

按日原子递增，避免并发重复单号：

| Redis Key | 单号格式 | 用途 |
|-----------|----------|------|
| `purchase_no:yyyyMMdd` | `CG-yyyyMMdd-001` | 采购单 |
| `inbound_no:yyyyMMdd` | `RK-yyyyMMdd-001` | 入库单 |
| `receive_no:yyyyMMdd` | `LY-yyyyMMdd-001` | 领用单 |

Redis 未启动时创建单据会返回 `50301`，由 `GlobalExceptionHandler` 统一处理。

### 3.1.3 角色权限工具（RoleAuthSupport）

```java
public final class RoleAuthSupport {
    // admin / gm 视为超级管理员，可豁免部分角色限制（联调）
    public static boolean isSuperManager(User user) { ... }

    // 采购员确认、仓管入库/出库等
    public static void assertAnyRole(User user, RoleEnum... roles) { ... }

    // 40301，附带当前角色中文名便于排查
    public static BusinessException forbidden(User user) { ... }
}
```

**系统角色**：`dept_employee`、`dept_head`、`purchaser`、`warehouse_keeper`、`finance`、`gm`、`admin`。  
其中 **部门员工/负责人** 必须绑定 `dept_id`；**采购员、仓管** 等职能角色可不绑部门。

### 3.1.4 采购服务（PurchaseServiceImpl）

核心能力：创建采购单、审批、采购确认、取消；状态机 `PurchaseStatusEnum`：

```text
1 待审批 → 2 已通过 → 3 采购中 → 4 已入库
         ↘ 6 已驳回    ↘ 5 已取消（无关联入库单时可取消）
```

创建采购单片段：

```java
@Transactional(rollbackFor = Exception.class)
public Map<String, Object> addPurchase(AddPurchaseDTO dto) {
    validatePurchaseHeader(dto);
    String purchaseNo = purchaseNoService.generatePurchaseNo();  // Redis
    // 写入 purchase + purchase_item，status = 1
    return Map.of("purchaseId", purchaseId, "purchaseNo", purchaseNo);
}
```

### 3.1.5 入库服务（InboundServiceImpl）

- **创建入库单**：关联已通过/采购中的采购单，校验剩余可入数量，状态 `1-待验收`，**此时不改库存**。
- **验收确认**：部门负责人 + 仓管角色校验；常规入库更新 `inventory` 移动加权平均；写 `inventory_record`；入库单 `2-已验收`；**采购单同步 `4-已入库`**。

```java
@Transactional(rollbackFor = Exception.class)
public void confirm(InboundConfirmDTO confirmDTO) {
    // 1. 校验待验收、验收人、仓管
    // 2. 逐明细：常规入库加库存 / 食材直拨只写流水
    // 3. 累加 purchase_item.received_quantity
    // 4. inbound.status = 2，purchase.status = 4
    updatePurchaseStatusAfterAcceptance(purchase.getId());
}
```

入库类型：`1` 常规入库（加库存）、`2` 食材直拨（不写库存，流水为负）。

### 3.1.6 领用服务（ReceiveServiceImpl）

- 创建领用单：申请人须与部门一致，快照 `inventory.unit_cost` 到明细。
- 审批：本部门 `dept_head` 或超级管理员。
- 确认出库：仓管扣减库存，写 `change_type=2` 正常出库流水，状态 `3-已出库`。

### 3.1.7 全局异常与错误码

`GlobalExceptionHandler` 统一返回 `Result`：

- `BusinessException` → 业务提示（如「只有待审批采购单可以审批」）
- `BizErrorCode.AUTH_FORBIDDEN (40301)` → 无权操作
- `BizErrorCode.REDIS_UNAVAILABLE (50301)` → Redis 连接失败
- 参数校验失败 → `400` + 字段提示

### 3.1.8 数据库设计（12 表）

| 表 | 说明 |
|----|------|
| `"user"` | 用户与角色 |
| `department` | 部门树 |
| `supplier` / `product` | 供应商、商品 |
| `purchase` / `purchase_item` | 采购单及明细（含已收数量） |
| `inbound` / `inbound_item` | 入库单及明细 |
| `receive` / `receive_item` | 领用单及明细 |
| `inventory` / `inventory_record` | 库存与流水 |

## 3.2 整体流程概览（快乐路径）

以下为链路集成测试 `PurchaseToReceiveHappyPathIntegrationTest` 对应的标准流程。

### Step 1：新增采购申请

**接口**：`POST /api/v1/purchase/add`

**请求要点**：`deptId`、`applyUserId`、`supplierId`、`items[{ productId, quantity, unitPrice }]`

**结果**：

```json
{
  "code": 200,
  "message": "申请成功",
  "data": { "purchaseId": 5, "purchaseNo": "CG-20260617-001" }
}
```

**状态**：采购单 `status = 1`（待审批）。

**实现**：Redis 生成单号；事务写入采购头与明细；金额 `BigDecimal` 汇总。

---

### Step 2：审批采购

**接口**：`POST /api/v1/purchase/approve`

**请求**：`purchaseId`、`approverUserId`（本部门 dept_head）、`approved: true`

**状态**：`1 → 2`（已通过）。

**实现**：`RoleAuthSupport` 校验审批人须为本部门负责人或超级管理员；跨部门返回 `40301`。

---

### Step 3：采购确认

**接口**：`POST /api/v1/purchase/confirm`

**请求**：`purchaseId`、`purchaserUserId`（purchaser 或 admin/gm）

**状态**：`2 → 3`（采购中）。

---

### Step 4：创建入库单

**接口**：`POST /api/v1/inbound/add`

**请求**：`purchaseId`、`receiverUserId`（warehouse_keeper）、`inboundType: 1`、`items[{ productId, actualQuantity }]`

**结果**：`inboundId`、`inboundNo`（`RK-...`）

**状态**：入库单 `1-待验收`；**库存尚未变化**。

---

### Step 5：验收确认

**接口**：`POST /api/v1/inbound/confirm`

**请求**：`inboundId`、`deptHeadUserId`、`warehouseKeeperUserId`

**状态**：

- 入库单 `1 → 2`（已验收）
- 采购单 `3 → 4`（已入库）
- 常规入库：库存 `+actualQuantity`，写入库流水

**日志/观测字段**（集成测试会打印）：

```text
[入库单-步骤5完成后] status=2(已验收)
[采购单-步骤5完成后] status=4(已入库)
[库存-步骤5完成后] stockQuantity=100
```

---

### Step 6：新增领用单

**接口**：`POST /api/v1/receive/add`

**请求**：`deptId`、`applyUserId`、`purpose`、`items[{ productId, quantity }]`

**状态**：领用单 `1-待审批`，单号 `LY-...`。

---

### Step 7：审批领用

**接口**：`POST /api/v1/receive/approve`

**状态**：`1 → 2`（待出库）。

---

### Step 8：确认出库

**接口**：`POST /api/v1/receive/confirm`

**请求**：`receiveId`、`delivererUserId`（warehouse_keeper）

**状态**：`2 → 3`（已出库）；库存扣减；写出库流水。

**闭环**：采购入库增加的库存，通过领用出库消耗，全链路可在 `inventory` / `inventory_record` 中追溯。

---

### 流程状态总览

```text
采购:  1待审批 → 2已通过 → 3采购中 → 4已入库
入库:  1待验收 → 2已验收
领用:  1待审批 → 2待出库 → 3已出库
```

## 3.3 部署与访问

| 组件 | 默认配置 |
|------|----------|
| Spring Boot | `http://localhost:8080` |
| Swagger | `/swagger-ui.html` |
| PostgreSQL | `localhost:5432/hotel_inventory`，用户 `postgres` |
| Redis | `localhost:6379` |
| Nginx 反向代理（可选） | `8081` → 转发至 `8080` |

启动顺序：**PostgreSQL → Redis → Spring Boot →（可选）Nginx**。

详细步骤见项目根目录 **[README.md](./README.md)**；测试基础数据见 **`sql/integration_test_seed_data.sql`**。

# 4. 项目演示

（可在交工演示时补充以下截图）

1. **Swagger 接口文档**：`http://localhost:8080/swagger-ui.html` 或经 Nginx `http://IP:8081/swagger-ui.html`
2. **采购单列表/详情**：展示 `purchaseNo`、状态流转
3. **入库验收前后**：`inbound.status` 与 `purchase.status` 同步变为 2 / 4
4. **库存与流水**：`inventory.stock_quantity`、`inventory_record` 入库/出库记录
5. **链路测试控制台日志**：运行 `PurchaseToReceiveHappyPathIntegrationTest` 输出的各步骤状态快照

---

**项目路径**：`D:\project\javaProjrct\FrManage`  
**文档索引**：`README.md`（部署）、`测试开发使用版本.md`（接口与建表）、`接口权限清单.md`（角色权限）、`日志.md`（迭代记录）
