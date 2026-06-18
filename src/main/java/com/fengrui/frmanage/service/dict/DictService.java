package com.fengrui.frmanage.service.dict;

import com.fengrui.frmanage.vo.dict.RoleDictVO;

import java.util.List;

/**
 * 字典数据服务，提供前端下拉所需的枚举选项。
 */
public interface DictService {

    /**
     * 查询商品分类字典。
     *
     * @return 商品分类名称列表
     */
    List<String> listCategories();

    /**
     * 查询系统角色字典。
     *
     * @return 角色编码与中文名称列表
     */
    List<RoleDictVO> listRoles();
}
