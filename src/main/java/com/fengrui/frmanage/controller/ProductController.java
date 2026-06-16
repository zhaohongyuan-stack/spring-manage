package com.fengrui.frmanage.controller;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.dto.product.AddProductDTO;
import com.fengrui.frmanage.dto.product.DeleteProductDTO;
import com.fengrui.frmanage.dto.product.ProductQueryDTO;
import com.fengrui.frmanage.dto.product.UpdateProductDTO;
import com.fengrui.frmanage.service.product.ProductService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.product.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品接口控制器。
 */
@RestController
@RequestMapping("/api/v1/product")
@Tag(name = "商品管理", description = "商品新增、查询、修改和删除接口")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 新增商品。
     *
     * @param addProductDTO 新增商品参数
     * @return 商品ID
     */
    @PostMapping("/add")
    // TODO 后续补充 @PreAuthorize("hasAnyRole('admin','warehouse_keeper')")
    @Operation(summary = "新增商品", description = "系统管理员或仓库管理员录入物资商品信息")
    public Result<Map<String, Long>> addProduct(@Valid @RequestBody AddProductDTO addProductDTO) {
        return Result.success("新增成功", Map.of("id", productService.addProduct(addProductDTO)));
    }

    /**
     * 分页查询商品。
     *
     * @param productQueryDTO 查询参数
     * @return 商品分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "商品列表查询", description = "分页查询商品，支持按名称和分类筛选")
    public Result<PageResultVO<ProductVO>> listProduct(@ModelAttribute ProductQueryDTO productQueryDTO) {
        return Result.success(productService.listProduct(productQueryDTO));
    }

    /**
     * 修改商品信息。
     *
     * @param updateProductDTO 修改商品参数
     * @return 空响应
     */
    @PostMapping("/update")
    // TODO 后续补充 @PreAuthorize("hasAnyRole('admin','warehouse_keeper')")
    @Operation(summary = "修改商品信息", description = "系统管理员或仓库管理员更新商品成本价和预警阈值")
    public Result<Void> updateProduct(@Valid @RequestBody UpdateProductDTO updateProductDTO) {
        productService.updateProduct(updateProductDTO);
        return Result.success("成功", null);
    }

    /**
     * 删除商品。
     *
     * @param deleteProductDTO 删除商品参数
     * @return 空响应
     */
    @PostMapping("/delete")
    // TODO 后续补充 @PreAuthorize("hasAnyRole('admin','warehouse_keeper')")
    @Operation(summary = "删除商品", description = "逻辑删除商品，将商品状态改为禁用")
    public Result<Void> deleteProduct(@Valid @RequestBody DeleteProductDTO deleteProductDTO) {
        productService.deleteProduct(deleteProductDTO);
        return Result.success("成功", null);
    }
}
