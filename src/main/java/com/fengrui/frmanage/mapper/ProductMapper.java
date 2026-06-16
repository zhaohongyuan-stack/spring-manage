package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengrui.frmanage.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品持久层接口。
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 查询启用商品分类字典。
     *
     * @return 分类名称列表
     */
    @Select("""
            SELECT DISTINCT category
            FROM product
            WHERE status = 1
              AND category IS NOT NULL
              AND category <> ''
            ORDER BY category
            """)
    List<String> selectEnabledCategories();
}
