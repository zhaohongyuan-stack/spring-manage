package com.fengrui.frmanage.service.product;

import com.fengrui.frmanage.dto.product.AddProductDTO;
import com.fengrui.frmanage.dto.product.DeleteProductDTO;
import com.fengrui.frmanage.dto.product.ProductQueryDTO;
import com.fengrui.frmanage.dto.product.UpdateProductDTO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.product.ProductVO;

/**
 * 商品业务服务。
 */
public interface ProductService {

    /**
     * 新增商品。
     *
     * @param addProductDTO 新增商品参数
     * @return 商品ID
     */
    Long addProduct(AddProductDTO addProductDTO);

    /**
     * 分页查询商品列表。
     *
     * @param productQueryDTO 查询参数
     * @return 商品分页数据
     */
    PageResultVO<ProductVO> listProduct(ProductQueryDTO productQueryDTO);

    /**
     * 修改商品信息。
     *
     * @param updateProductDTO 修改商品参数
     */
    void updateProduct(UpdateProductDTO updateProductDTO);

    /**
     * 禁用商品。
     *
     * @param deleteProductDTO 删除商品参数
     */
    void deleteProduct(DeleteProductDTO deleteProductDTO);
}
