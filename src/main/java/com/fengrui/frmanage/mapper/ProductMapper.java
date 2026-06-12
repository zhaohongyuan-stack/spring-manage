package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengrui.frmanage.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品持久层接口。
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
