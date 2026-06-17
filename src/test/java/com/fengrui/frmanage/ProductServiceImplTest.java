package com.fengrui.frmanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fengrui.frmanage.dto.product.AddProductDTO;
import com.fengrui.frmanage.entity.Product;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.ProductMapper;
import com.fengrui.frmanage.service.impl.product.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 商品业务服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productMapper);
    }

    @Test
    void addProductShouldThrowWhenNameSpecDuplicated() {
        when(productMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        AddProductDTO addProductDTO = buildAddProductDTO();

        BusinessException exception = assertThrows(BusinessException.class, () -> productService.addProduct(addProductDTO));
        assertEquals("商品名称与规格组合已存在", exception.getMessage());
        verify(productMapper, never()).insert(any(Product.class));
    }

    @Test
    void addProductShouldInsertWhenNameSpecUnique() {
        when(productMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(productMapper.insert(any(Product.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Product.class).setId(2L);
            return 1;
        });

        Long productId = productService.addProduct(buildAddProductDTO());

        assertEquals(2L, productId);
        verify(productMapper).insert(any(Product.class));
    }

    /**
     * 构建新增商品测试参数。
     *
     * @return 新增商品参数
     */
    private AddProductDTO buildAddProductDTO() {
        AddProductDTO addProductDTO = new AddProductDTO();
        addProductDTO.setProductName("瓶装洗发水");
        addProductDTO.setCategory("客房用品");
        addProductDTO.setSpec("320ml/瓶");
        addProductDTO.setUnit("瓶");
        addProductDTO.setCostPrice(new BigDecimal("4.10"));
        addProductDTO.setWarningThreshold(30);
        return addProductDTO;
    }
}
