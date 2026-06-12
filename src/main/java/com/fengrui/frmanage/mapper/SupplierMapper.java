package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengrui.frmanage.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商持久层接口。
 */
@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {
}
