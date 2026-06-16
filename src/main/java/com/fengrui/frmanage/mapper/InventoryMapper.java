package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengrui.frmanage.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存持久层接口。
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
}
