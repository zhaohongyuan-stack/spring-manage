-- =============================================================================
-- FrManage 链路测试基础数据（PostgreSQL）
-- 用途：PurchaseToReceiveHappyPathIntegrationTest 及手工联调
-- 执行库：hotel_inventory
-- 默认密码：123456（BCrypt 加密，与 Spring Security BCryptPasswordEncoder 一致）
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. 部门（dept_id = 1）
-- -----------------------------------------------------------------------------
INSERT INTO department (id, dept_name, parent_id, status, create_time)
VALUES (1, '客房部', 0, 1, NOW())
ON CONFLICT (id) DO UPDATE
SET dept_name   = EXCLUDED.dept_name,
    parent_id   = EXCLUDED.parent_id,
    status      = EXCLUDED.status;

-- -----------------------------------------------------------------------------
-- 2. 供应商（至少一条；链路测试取第一条）
-- -----------------------------------------------------------------------------
INSERT INTO supplier (id, supplier_name, contact_person, contact_phone, address, status, create_time)
VALUES (1, '沈阳测试酒店用品公司', '张经理', '13800000001', '沈阳市和平区测试路1号', 1, NOW())
ON CONFLICT (id) DO UPDATE
SET supplier_name  = EXCLUDED.supplier_name,
    contact_person = EXCLUDED.contact_person,
    contact_phone  = EXCLUDED.contact_phone,
    address        = EXCLUDED.address,
    status         = EXCLUDED.status;

-- -----------------------------------------------------------------------------
-- 3. 测试用户
--    100 dept_employee  申请人
--    101 dept_head      部门负责人（审批/验收）
--    102 purchaser      采购员
--    103 warehouse_keeper 仓库管理员（入库/出库，测试自动解析）
-- -----------------------------------------------------------------------------
INSERT INTO "user" (id, username, password, real_name, role, dept_id, phone, status, create_time, update_time)
VALUES
    (100, 'test_employee', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHM8lE9lBOsl7iwK8HJ/2', '测试员工', 'dept_employee', 1, '13800000100', 1, NOW(), NOW()),
    (101, 'test_dept_head', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHM8lE9lBOsl7iwK8HJ/2', '测试部门负责人', 'dept_head', 1, '13800000101', 1, NOW(), NOW()),
    (102, 'test_purchaser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHM8lE9lBOsl7iwK8HJ/2', '测试采购员', 'purchaser', NULL, '13800000102', 1, NOW(), NOW()),
    (103, 'test_warehouse', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHM8lE9lBOsl7iwK8HJ/2', '测试仓管', 'warehouse_keeper', 1, '13800000103', 1, NOW(), NOW())
ON CONFLICT (id) DO UPDATE
SET username   = EXCLUDED.username,
    password   = EXCLUDED.password,
    real_name  = EXCLUDED.real_name,
    role       = EXCLUDED.role,
    dept_id    = EXCLUDED.dept_id,
    phone      = EXCLUDED.phone,
    status     = EXCLUDED.status,
    update_time = NOW();

-- -----------------------------------------------------------------------------
-- 4. 商品 301（启用；链路测试 productId=301）
-- -----------------------------------------------------------------------------
INSERT INTO product (id, product_name, category, spec, unit, cost_price, warning_threshold, status, create_time, update_time)
VALUES (301, '一次性牙具套装', '客房用品', '标准装', '套', 2.20, 50, 1, NOW(), NOW())
ON CONFLICT (id) DO UPDATE
SET product_name      = EXCLUDED.product_name,
    category          = EXCLUDED.category,
    spec              = EXCLUDED.spec,
    unit              = EXCLUDED.unit,
    cost_price        = EXCLUDED.cost_price,
    warning_threshold = EXCLUDED.warning_threshold,
    status            = EXCLUDED.status,
    update_time       = NOW();

-- -----------------------------------------------------------------------------
-- 5. 同步自增序列（避免后续 INSERT 主键冲突）
-- -----------------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('department', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM department), 1));
SELECT setval(pg_get_serial_sequence('supplier', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM supplier), 1));
SELECT setval(pg_get_serial_sequence('"user"', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM "user"), 103));
SELECT setval(pg_get_serial_sequence('product', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM product), 301));

COMMIT;

-- -----------------------------------------------------------------------------
-- 验证查询（可选）
-- -----------------------------------------------------------------------------
-- SELECT id, username, role, dept_id, status FROM "user" WHERE id IN (100,101,102,103) ORDER BY id;
-- SELECT id, product_name, cost_price, status FROM product WHERE id = 301;
-- SELECT id, supplier_name, status FROM supplier WHERE id = 1;
