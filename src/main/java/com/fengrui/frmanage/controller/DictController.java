package com.fengrui.frmanage.controller;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.service.dict.DictService;
import com.fengrui.frmanage.vo.dict.RoleDictVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典接口控制器，提供前端下拉所需的枚举数据。
 */
@RestController
@RequestMapping("/api/v1/dict")
@Tag(name = "字典管理", description = "商品分类、角色等枚举字典查询")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    /**
     * 查询商品分类字典。
     *
     * @return 商品分类名称列表
     */
    @GetMapping("/categories")
    @Operation(summary = "商品分类字典", description = "返回预定义商品分类字符串数组，供商品表单下拉使用；展示与提交均使用分类中文名")
    public Result<List<String>> listCategories() {
        return Result.success(dictService.listCategories());
    }

    /**
     * 查询系统角色字典。
     *
     * @return 角色编码与中文名称列表
     */
    @GetMapping("/roles")
    @Operation(summary = "角色字典", description = "返回系统角色 code/name 列表；前端下拉展示 name，提交传 code")
    public Result<List<RoleDictVO>> listRoles() {
        return Result.success(dictService.listRoles());
    }
}
