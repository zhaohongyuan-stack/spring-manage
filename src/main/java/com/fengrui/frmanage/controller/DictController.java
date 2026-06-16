package com.fengrui.frmanage.controller;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.mapper.ProductMapper;
import com.fengrui.frmanage.vo.dict.DictItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 通用字典接口控制器。
 */
@RestController
@RequestMapping("/api/v1/dict")
@Tag(name = "通用字典", description = "前端下拉选项字典接口")
public class DictController {

    private final ProductMapper productMapper;

    public DictController(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /**
     * 查询商品分类字典。
     *
     * @return 商品分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "商品分类字典", description = "查询启用商品中已存在的分类")
    public Result<List<String>> categories() {
        return Result.success(productMapper.selectEnabledCategories());
    }

    /**
     * 查询角色字典。
     *
     * @return 角色字典列表
     */
    @GetMapping("/roles")
    @Operation(summary = "角色字典", description = "查询系统内置角色编码和名称")
    public Result<List<DictItemVO>> roles() {
        List<DictItemVO> roles = Arrays.stream(RoleEnum.values())
                .map(role -> new DictItemVO(role.getCode(), role.getName()))
                .toList();
        return Result.success(roles);
    }
}
