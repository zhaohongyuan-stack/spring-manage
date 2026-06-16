package com.fengrui.frmanage;

import com.fengrui.frmanage.config.SecurityConfig;
import com.fengrui.frmanage.controller.ProductController;
import com.fengrui.frmanage.dto.product.ProductQueryDTO;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import com.fengrui.frmanage.service.product.ProductService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.product.ProductVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 商品接口控制器测试。
 */
@WebMvcTest(ProductController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void addProductShouldReturnProductIdWithoutAuthenticationInTemporaryPermitMode() throws Exception {
        when(productService.addProduct(any())).thenReturn(2L);

        mockMvc.perform(post("/api/v1/product/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productName": "瓶装洗发水",
                                  "category": "客房用品",
                                  "spec": "300ml/瓶",
                                  "unit": "瓶",
                                  "costPrice": 4.10,
                                  "warningThreshold": 30
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("新增成功"))
                .andExpect(jsonPath("$.data.id").value(2));
    }

    @Test
    void addProductShouldValidateRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/product/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productName": "",
                                  "category": "",
                                  "costPrice": null,
                                  "warningThreshold": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("商品名称不能为空")))
                .andExpect(jsonPath("$.message").value(containsString("商品分类不能为空")))
                .andExpect(jsonPath("$.message").value(containsString("成本单价不能为空")))
                .andExpect(jsonPath("$.message").value(containsString("预警阈值不能为空")));
    }

    @Test
    void listProductShouldReturnEnabledProductPage() throws Exception {
        ProductVO productVO = new ProductVO();
        productVO.setProductId(1L);
        productVO.setProductName("一次性牙具套装");
        productVO.setCategory("客房用品");
        productVO.setSpec("6g牙膏+软毛牙刷");
        productVO.setUnit("套");
        productVO.setCostPrice(new BigDecimal("2.20"));
        productVO.setWarningThreshold(50);
        productVO.setStatus((short) 1);
        productVO.setCreateTime(LocalDateTime.of(2026, 6, 1, 8, 0));
        when(productService.listProduct(any()))
                .thenReturn(new PageResultVO<>(1L, 1L, 10L, List.of(productVO)));

        mockMvc.perform(get("/api/v1/product/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].productId").value(1))
                .andExpect(jsonPath("$.data.list[0].productName").value("一次性牙具套装"));
    }

    @Test
    void listProductShouldBindProductNameAndCategoryQuery() throws Exception {
        when(productService.listProduct(any()))
                .thenReturn(new PageResultVO<>(0L, 2L, 5L, List.of()));

        mockMvc.perform(get("/api/v1/product/list")
                        .param("pageNum", "2")
                        .param("pageSize", "5")
                        .param("productName", "牙具")
                        .param("category", "客房用品"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<ProductQueryDTO> queryCaptor = ArgumentCaptor.forClass(ProductQueryDTO.class);
        verify(productService).listProduct(queryCaptor.capture());
        assertEquals(2L, queryCaptor.getValue().getPageNum());
        assertEquals(5L, queryCaptor.getValue().getPageSize());
        assertEquals("牙具", queryCaptor.getValue().getProductName());
        assertEquals("客房用品", queryCaptor.getValue().getCategory());
    }

    @Test
    void updateProductShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/product/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 2,
                                  "costPrice": 4.30,
                                  "warningThreshold": 25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void updateProductShouldReturnBusinessErrorWhenProductNotFound() throws Exception {
        doThrow(new BusinessException("商品不存在")).when(productService).updateProduct(any());

        mockMvc.perform(post("/api/v1/product/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 999,
                                  "costPrice": 4.30,
                                  "warningThreshold": 25
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("商品不存在"));
    }

    @Test
    void deleteProductShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/product/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteProductShouldValidateProductId() throws Exception {
        mockMvc.perform(post("/api/v1/product/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("商品ID不能为空")));
    }
}
