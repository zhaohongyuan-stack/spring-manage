-- 领用单号唯一索引，防止并发场景产生重复 receive_no。
CREATE UNIQUE INDEX IF NOT EXISTS uk_receive_receive_no ON receive (receive_no);
