package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengrui.frmanage.entity.Purchase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购单持久层接口。
 */
@Mapper
public interface PurchaseMapper extends BaseMapper<Purchase> {
}
