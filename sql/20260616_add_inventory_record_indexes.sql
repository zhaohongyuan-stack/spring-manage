-- 库存流水查询索引，优化商品、时间和关联单号筛选。
CREATE INDEX IF NOT EXISTS idx_inventory_record_product_id ON inventory_record (product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_record_create_time ON inventory_record (create_time);
CREATE INDEX IF NOT EXISTS idx_inventory_record_related_no ON inventory_record (related_no);
