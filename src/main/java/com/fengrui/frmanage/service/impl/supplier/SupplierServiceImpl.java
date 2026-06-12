package com.fengrui.frmanage.service.impl.supplier;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.frmanage.common.enums.SupplierStatusEnum;
import com.fengrui.frmanage.dto.supplier.AddSupplierDTO;
import com.fengrui.frmanage.dto.supplier.DeleteSupplierDTO;
import com.fengrui.frmanage.dto.supplier.SupplierQueryDTO;
import com.fengrui.frmanage.entity.Supplier;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.SupplierMapper;
import com.fengrui.frmanage.service.supplier.SupplierService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.supplier.SupplierVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 供应商业务服务实现。
 */
@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierMapper supplierMapper;

    public SupplierServiceImpl(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    /**
     * 新增供应商。
     *
     * @param addSupplierDTO 新增供应商参数
     * @return 供应商ID
     */
    @Override
    public Long addSupplier(AddSupplierDTO addSupplierDTO) {
        validateSupplierNameUnique(addSupplierDTO.getSupplierName());

        Supplier supplier = new Supplier();
        supplier.setSupplierName(addSupplierDTO.getSupplierName());
        supplier.setContactPerson(addSupplierDTO.getContactPerson());
        supplier.setContactPhone(addSupplierDTO.getContactPhone());
        supplier.setAddress(addSupplierDTO.getAddress());
        supplier.setStatus(SupplierStatusEnum.ENABLED.getCode().shortValue());
        supplierMapper.insert(supplier);
        return supplier.getId();
    }

    /**
     * 删除供应商。
     *
     * @param deleteSupplierDTO 删除供应商参数
     */
    @Override
    public void deleteSupplier(DeleteSupplierDTO deleteSupplierDTO) {
        Supplier supplier = supplierMapper.selectById(deleteSupplierDTO.getId());
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }
        supplierMapper.deleteById(deleteSupplierDTO.getId());
    }

    /**
     * 分页查询供应商列表。
     *
     * @param supplierQueryDTO 查询参数
     * @return 供应商分页数据
     */
    @Override
    public PageResultVO<SupplierVO> listSupplier(SupplierQueryDTO supplierQueryDTO) {
        LambdaQueryWrapper<Supplier> queryWrapper = new LambdaQueryWrapper<Supplier>()
                .like(StringUtils.hasText(supplierQueryDTO.getSupplierName()),
                        Supplier::getSupplierName,
                        supplierQueryDTO.getSupplierName())
                .orderByDesc(Supplier::getId);
        IPage<Supplier> supplierPage = supplierMapper.selectPage(
                Page.of(supplierQueryDTO.getPageNum(), supplierQueryDTO.getPageSize()),
                queryWrapper
        );
        List<SupplierVO> supplierList = supplierPage.getRecords()
                .stream()
                .map(this::toSupplierVO)
                .toList();
        return new PageResultVO<>(
                supplierPage.getTotal(),
                supplierPage.getCurrent(),
                supplierPage.getSize(),
                supplierList
        );
    }

    /**
     * 校验供应商名称唯一。
     *
     * @param supplierName 供应商名称
     */
    private void validateSupplierNameUnique(String supplierName) {
        Long sameNameCount = supplierMapper.selectCount(
                new LambdaQueryWrapper<Supplier>().eq(Supplier::getSupplierName, supplierName)
        );
        if (sameNameCount > 0) {
            throw new BusinessException("供应商名称已存在");
        }
    }

    /**
     * 转换供应商列表响应数据。
     *
     * @param supplier 供应商实体
     * @return 供应商列表响应数据
     */
    private SupplierVO toSupplierVO(Supplier supplier) {
        SupplierVO supplierVO = new SupplierVO();
        supplierVO.setSupplierId(supplier.getId());
        supplierVO.setSupplierName(supplier.getSupplierName());
        supplierVO.setContactPerson(supplier.getContactPerson());
        supplierVO.setContactPhone(supplier.getContactPhone());
        supplierVO.setAddress(supplier.getAddress());
        supplierVO.setStatus(supplier.getStatus());
        supplierVO.setCreateTime(supplier.getCreateTime());
        return supplierVO;
    }
}
