package com.fengrui.frmanage.service.supplier;

import com.fengrui.frmanage.dto.supplier.AddSupplierDTO;
import com.fengrui.frmanage.dto.supplier.DeleteSupplierDTO;
import com.fengrui.frmanage.dto.supplier.SupplierQueryDTO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.supplier.SupplierVO;

/**
 * 供应商业务服务。
 */
public interface SupplierService {

    /**
     * 新增供应商。
     *
     * @param addSupplierDTO 新增供应商参数
     * @return 供应商ID
     */
    Long addSupplier(AddSupplierDTO addSupplierDTO);

    /**
     * 删除供应商。
     *
     * @param deleteSupplierDTO 删除供应商参数
     */
    void deleteSupplier(DeleteSupplierDTO deleteSupplierDTO);

    /**
     * 分页查询供应商列表。
     *
     * @param supplierQueryDTO 查询参数
     * @return 供应商分页数据
     */
    PageResultVO<SupplierVO> listSupplier(SupplierQueryDTO supplierQueryDTO);
}
