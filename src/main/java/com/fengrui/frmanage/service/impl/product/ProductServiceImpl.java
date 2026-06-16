package com.fengrui.frmanage.service.impl.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.frmanage.common.enums.ProductStatusEnum;
import com.fengrui.frmanage.dto.product.AddProductDTO;
import com.fengrui.frmanage.dto.product.DeleteProductDTO;
import com.fengrui.frmanage.dto.product.ProductQueryDTO;
import com.fengrui.frmanage.dto.product.UpdateProductDTO;
import com.fengrui.frmanage.entity.Product;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.ProductMapper;
import com.fengrui.frmanage.service.product.ProductService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.product.ProductVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品业务服务实现。
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /**
     * 新增商品。
     *
     * @param addProductDTO 新增商品参数
     * @return 商品ID
     */
    @Override
    public Long addProduct(AddProductDTO addProductDTO) {
        Product product = new Product();
        product.setProductName(addProductDTO.getProductName());
        product.setCategory(addProductDTO.getCategory());
        product.setSpec(addProductDTO.getSpec());
        product.setUnit(addProductDTO.getUnit());
        product.setCostPrice(addProductDTO.getCostPrice());
        product.setWarningThreshold(addProductDTO.getWarningThreshold());
        product.setStatus(ProductStatusEnum.ENABLED.getCode().shortValue());
        productMapper.insert(product);
        return product.getId();
    }

    /**
     * 分页查询商品列表。
     *
     * @param productQueryDTO 查询参数
     * @return 商品分页数据
     */
    @Override
    public PageResultVO<ProductVO> listProduct(ProductQueryDTO productQueryDTO) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, ProductStatusEnum.ENABLED.getCode().shortValue())
                .like(StringUtils.hasText(productQueryDTO.getProductName()),
                        Product::getProductName,
                        productQueryDTO.getProductName())
                .eq(StringUtils.hasText(productQueryDTO.getCategory()),
                        Product::getCategory,
                        productQueryDTO.getCategory())
                .orderByDesc(Product::getId);
        IPage<Product> productPage = productMapper.selectPage(
                Page.of(productQueryDTO.getPageNum(), productQueryDTO.getPageSize()),
                queryWrapper
        );
        List<ProductVO> productList = productPage.getRecords()
                .stream()
                .map(this::toProductVO)
                .toList();
        return new PageResultVO<>(
                productPage.getTotal(),
                productPage.getCurrent(),
                productPage.getSize(),
                productList
        );
    }

    /**
     * 修改商品信息。
     *
     * @param updateProductDTO 修改商品参数
     */
    @Override
    public void updateProduct(UpdateProductDTO updateProductDTO) {
        if (updateProductDTO.getCostPrice() == null && updateProductDTO.getWarningThreshold() == null) {
            throw new BusinessException("至少修改一项商品信息");
        }
        Product existsProduct = getExistingProduct(updateProductDTO.getProductId());
        Product product = new Product();
        product.setId(existsProduct.getId());
        product.setCostPrice(updateProductDTO.getCostPrice());
        product.setWarningThreshold(updateProductDTO.getWarningThreshold());
        productMapper.updateById(product);
    }

    /**
     * 禁用商品。
     *
     * @param deleteProductDTO 删除商品参数
     */
    @Override
    public void deleteProduct(DeleteProductDTO deleteProductDTO) {
        Product existsProduct = getExistingProduct(deleteProductDTO.getProductId());
        Product product = new Product();
        product.setId(existsProduct.getId());
        product.setStatus(ProductStatusEnum.DISABLED.getCode().shortValue());
        productMapper.updateById(product);
    }

    /**
     * 获取已存在的商品。
     *
     * @param productId 商品ID
     * @return 商品实体
     */
    private Product getExistingProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        Short disabledStatus = ProductStatusEnum.DISABLED.getCode().shortValue();
        if (product == null || disabledStatus.equals(product.getStatus())) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    /**
     * 转换商品列表响应数据。
     *
     * @param product 商品实体
     * @return 商品列表响应数据
     */
    private ProductVO toProductVO(Product product) {
        ProductVO productVO = new ProductVO();
        productVO.setProductId(product.getId());
        productVO.setProductName(product.getProductName());
        productVO.setCategory(product.getCategory());
        productVO.setSpec(product.getSpec());
        productVO.setUnit(product.getUnit());
        productVO.setCostPrice(product.getCostPrice());
        productVO.setWarningThreshold(product.getWarningThreshold());
        productVO.setStatus(product.getStatus());
        productVO.setCreateTime(product.getCreateTime());
        return productVO;
    }
}
