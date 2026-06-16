-- 采购单号唯一索引，防止并发场景产生重复 purchase_no。
CREATE UNIQUE INDEX IF NOT EXISTS uk_purchase_purchase_no ON purchase (purchase_no);
