-- 入库单号唯一索引，防止并发场景产生重复 inbound_no。
CREATE UNIQUE INDEX IF NOT EXISTS uk_inbound_inbound_no ON inbound (inbound_no);
