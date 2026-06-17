-- 启用状态商品按「名称 + 规格」组合唯一，规格为空时按空字符串参与唯一约束。
CREATE UNIQUE INDEX IF NOT EXISTS uk_product_name_spec_enabled
    ON product (product_name, COALESCE(spec, ''))
    WHERE status = 1;
