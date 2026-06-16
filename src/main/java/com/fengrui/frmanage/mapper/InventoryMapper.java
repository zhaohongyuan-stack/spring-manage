package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.frmanage.entity.Inventory;
import com.fengrui.frmanage.vo.inventory.InventoryVO;
import com.fengrui.frmanage.vo.inventory.InventoryWarningVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 库存持久层接口。
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 分页查询当前库存快照。
     *
     * @param page 分页参数
     * @param productName 商品名称
     * @param category 商品分类
     * @return 库存分页数据
     */
    @Select("""
            <script>
            SELECT
                p.id AS product_id,
                p.product_name,
                p.category,
                p.spec,
                p.unit,
                p.warning_threshold,
                COALESCE(i.stock_quantity, 0) AS stock_quantity,
                COALESCE(i.unit_cost, 0) AS unit_cost,
                COALESCE(i.total_cost, 0) AS total_cost,
                i.last_update_time
            FROM product p
            LEFT JOIN inventory i ON i.product_id = p.id
            WHERE p.status = 1
            <if test="productName != null and productName != ''">
                AND p.product_name LIKE CONCAT('%', #{productName}, '%')
            </if>
            <if test="category != null and category != ''">
                AND p.category = #{category}
            </if>
            ORDER BY p.id DESC
            </script>
            """)
    IPage<InventoryVO> selectInventoryPage(Page<InventoryVO> page,
                                           @Param("productName") String productName,
                                           @Param("category") String category);

    /**
     * 分页查询库存预警商品。
     *
     * @param page 分页参数
     * @param productName 商品名称
     * @param category 商品分类
     * @return 库存预警分页数据
     */
    @Select("""
            <script>
            SELECT
                p.id AS product_id,
                p.product_name,
                p.category,
                p.spec,
                p.unit,
                p.warning_threshold,
                COALESCE(i.stock_quantity, 0) AS stock_quantity,
                ((p.warning_threshold - COALESCE(i.stock_quantity, 0)) + p.warning_threshold) AS suggest_quantity
            FROM product p
            LEFT JOIN inventory i ON i.product_id = p.id
            WHERE p.status = 1
              AND p.warning_threshold &gt; 0
              AND COALESCE(i.stock_quantity, 0) &lt; p.warning_threshold
            <if test="productName != null and productName != ''">
                AND p.product_name LIKE CONCAT('%', #{productName}, '%')
            </if>
            <if test="category != null and category != ''">
                AND p.category = #{category}
            </if>
            ORDER BY p.id DESC
            </script>
            """)
    IPage<InventoryWarningVO> selectWarningPage(Page<InventoryWarningVO> page,
                                                @Param("productName") String productName,
                                                @Param("category") String category);
}
