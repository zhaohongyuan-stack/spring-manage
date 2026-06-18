package com.fengrui.frmanage.service.impl.dict;

import com.fengrui.frmanage.common.enums.ProductCategoryEnum;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.service.dict.DictService;
import com.fengrui.frmanage.vo.dict.RoleDictVO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 字典数据服务实现，从系统枚举组装前端下拉选项。
 */
@Service
public class DictServiceImpl implements DictService {

    /**
     * 查询商品分类字典。
     *
     * @return 商品分类名称列表
     */
    @Override
    public List<String> listCategories() {
        return ProductCategoryEnum.listAllNames();
    }

    /**
     * 查询系统角色字典。
     *
     * @return 角色编码与中文名称列表
     */
    @Override
    public List<RoleDictVO> listRoles() {
        return Arrays.stream(RoleEnum.values())
                .map(role -> new RoleDictVO(role.getCode(), role.getName()))
                .toList();
    }
}
