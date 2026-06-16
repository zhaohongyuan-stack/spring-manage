package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengrui.frmanage.entity.InventoryRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水持久层接口。
 */
@Mapper
public interface InventoryRecordMapper extends BaseMapper<InventoryRecord> {
}
