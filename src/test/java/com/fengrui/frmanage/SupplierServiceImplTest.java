package com.fengrui.frmanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fengrui.frmanage.common.enums.BizErrorCode;
import com.fengrui.frmanage.dto.supplier.DeleteSupplierDTO;
import com.fengrui.frmanage.entity.Supplier;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.PurchaseMapper;
import com.fengrui.frmanage.mapper.SupplierMapper;
import com.fengrui.frmanage.service.impl.supplier.SupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 供应商业务服务测试。
 */
@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private PurchaseMapper purchaseMapper;

    private SupplierServiceImpl supplierService;

    @BeforeEach
    void setUp() {
        supplierService = new SupplierServiceImpl(supplierMapper, purchaseMapper);
    }

    @Test
    void deleteSupplierShouldRejectWhenReferencedByPurchase() {
        when(supplierMapper.selectById(3L)).thenReturn(new Supplier());
        when(purchaseMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> supplierService.deleteSupplier(buildDeleteSupplierDTO())
        );

        assertEquals(BizErrorCode.SUPPLIER_REFERENCED.getCode(), exception.getCode());
        assertEquals("供应商已被采购单引用，无法删除", exception.getMessage());
        verify(supplierMapper, never()).deleteById(3L);
    }

    private DeleteSupplierDTO buildDeleteSupplierDTO() {
        DeleteSupplierDTO deleteSupplierDTO = new DeleteSupplierDTO();
        deleteSupplierDTO.setId(3L);
        return deleteSupplierDTO;
    }
}
