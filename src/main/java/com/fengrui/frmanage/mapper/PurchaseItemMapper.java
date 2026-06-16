package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengrui.frmanage.entity.PurchaseItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购明细持久层接口。
 */
@Mapper
public interface PurchaseItemMapper extends BaseMapper<PurchaseItem> {
}
