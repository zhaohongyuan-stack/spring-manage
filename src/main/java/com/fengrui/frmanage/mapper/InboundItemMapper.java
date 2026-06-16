package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengrui.frmanage.entity.InboundItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库明细持久层接口。
 */
@Mapper
public interface InboundItemMapper extends BaseMapper<InboundItem> {
}
