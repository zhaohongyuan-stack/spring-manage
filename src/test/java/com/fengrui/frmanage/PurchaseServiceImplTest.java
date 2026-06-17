package com.fengrui.frmanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fengrui.frmanage.common.enums.BizErrorCode;
import com.fengrui.frmanage.common.enums.ProductStatusEnum;
import com.fengrui.frmanage.common.enums.PurchaseStatusEnum;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.dto.purchase.AddPurchaseDTO;
import com.fengrui.frmanage.dto.purchase.AddPurchaseItemDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseApproveDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseCancelDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseConfirmDTO;
import com.fengrui.frmanage.entity.Department;
import com.fengrui.frmanage.entity.Product;
import com.fengrui.frmanage.entity.Purchase;
import com.fengrui.frmanage.entity.PurchaseItem;
import com.fengrui.frmanage.entity.Supplier;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.DepartmentMapper;
import com.fengrui.frmanage.mapper.InboundMapper;
import com.fengrui.frmanage.mapper.ProductMapper;
import com.fengrui.frmanage.mapper.PurchaseItemMapper;
import com.fengrui.frmanage.mapper.PurchaseMapper;
import com.fengrui.frmanage.mapper.SupplierMapper;
import com.fengrui.frmanage.mapper.UserMapper;
import com.fengrui.frmanage.service.impl.purchase.PurchaseServiceImpl;
import com.fengrui.frmanage.service.purchase.PurchaseNoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 采购业务服务测试。
 */
@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseMapper purchaseMapper;

    @Mock
    private PurchaseItemMapper purchaseItemMapper;

    @Mock
    private InboundMapper inboundMapper;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private PurchaseNoService purchaseNoService;

    private PurchaseServiceImpl purchaseService;

    @BeforeEach
    void setUp() {
        purchaseService = new PurchaseServiceImpl(
                purchaseMapper,
                purchaseItemMapper,
                inboundMapper,
                departmentMapper,
                userMapper,
                supplierMapper,
                productMapper,
                purchaseNoService
        );
    }

    @Test
    void addPurchaseShouldThrowWhenDeptNotFound() {
        when(departmentMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> purchaseService.addPurchase(buildAddPurchaseDTO()));
        assertEquals(BizErrorCode.PURCHASE_DEPT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals("申请部门不存在", exception.getMessage());
    }

    @Test
    void addPurchaseShouldThrowWhenApplyUserDisabled() {
        when(departmentMapper.selectById(1L)).thenReturn(new Department());
        User disabledUser = new User();
        disabledUser.setId(1001L);
        disabledUser.setStatus((short) 0);
        when(userMapper.selectById(1001L)).thenReturn(disabledUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> purchaseService.addPurchase(buildAddPurchaseDTO()));
        assertEquals(BizErrorCode.PURCHASE_USER_DISABLED.getCode(), exception.getCode());
        assertEquals("申请人已禁用", exception.getMessage());
    }

    @Test
    void addPurchaseShouldInsertHeaderAndItems() {
        when(departmentMapper.selectById(1L)).thenReturn(new Department());
        when(userMapper.selectById(1001L)).thenReturn(new User());
        when(supplierMapper.selectById(3L)).thenReturn(new Supplier());
        when(purchaseNoService.generatePurchaseNo()).thenReturn("CG-20260616-001");
        Product product = new Product();
        product.setId(1L);
        product.setProductName("一次性牙具套装");
        product.setStatus(ProductStatusEnum.ENABLED.getCode().shortValue());
        when(productMapper.selectById(1L)).thenReturn(product);
        when(purchaseMapper.insert(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase purchase = invocation.getArgument(0);
            purchase.setId(101L);
            return 1;
        });

        AddPurchaseDTO addPurchaseDTO = buildAddPurchaseDTO();
        purchaseService.addPurchase(addPurchaseDTO);

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        ArgumentCaptor<PurchaseItem> itemCaptor = ArgumentCaptor.forClass(PurchaseItem.class);
        verify(purchaseMapper).insert(purchaseCaptor.capture());
        verify(purchaseItemMapper).insert(itemCaptor.capture());
        assertEquals("CG-20260616-001", purchaseCaptor.getValue().getPurchaseNo());
        assertEquals(new BigDecimal("220.00"), purchaseCaptor.getValue().getTotalAmount());
        assertEquals(PurchaseStatusEnum.PENDING_APPROVAL.getCode(), purchaseCaptor.getValue().getStatus());
        assertEquals(101L, itemCaptor.getValue().getPurchaseId());
        assertEquals(0, itemCaptor.getValue().getReceivedQuantity());
    }

    @Test
    void approveShouldRejectWhenStatusNotPending() {
        Purchase purchase = new Purchase();
        purchase.setId(101L);
        purchase.setStatus(PurchaseStatusEnum.APPROVED.getCode());
        when(purchaseMapper.selectById(101L)).thenReturn(purchase);

        PurchaseApproveDTO approveDTO = new PurchaseApproveDTO();
        approveDTO.setPurchaseId(101L);
        approveDTO.setApproverUserId(1005L);
        approveDTO.setApproved(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> purchaseService.approve(approveDTO));
        assertEquals("只有待审批采购单可以审批", exception.getMessage());
    }

    @Test
    void approveShouldRequireRemarkWhenRejected() {
        Purchase purchase = new Purchase();
        purchase.setId(101L);
        purchase.setDeptId(1L);
        purchase.setStatus(PurchaseStatusEnum.PENDING_APPROVAL.getCode());
        when(purchaseMapper.selectById(101L)).thenReturn(purchase);
        when(userMapper.selectById(1005L)).thenReturn(buildUser(1005L, 1L, RoleEnum.DEPT_HEAD.getCode()));

        PurchaseApproveDTO approveDTO = new PurchaseApproveDTO();
        approveDTO.setPurchaseId(101L);
        approveDTO.setApproverUserId(1005L);
        approveDTO.setApproved(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> purchaseService.approve(approveDTO));
        assertEquals("驳回时必须填写驳回理由", exception.getMessage());
    }

    @Test
    void approveShouldRejectWhenDeptHeadFromOtherDept() {
        Purchase purchase = new Purchase();
        purchase.setId(101L);
        purchase.setDeptId(1L);
        purchase.setStatus(PurchaseStatusEnum.PENDING_APPROVAL.getCode());
        when(purchaseMapper.selectById(101L)).thenReturn(purchase);
        when(userMapper.selectById(1005L)).thenReturn(buildUser(1005L, 2L, RoleEnum.DEPT_HEAD.getCode()));

        PurchaseApproveDTO approveDTO = new PurchaseApproveDTO();
        approveDTO.setPurchaseId(101L);
        approveDTO.setApproverUserId(1005L);
        approveDTO.setApproved(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> purchaseService.approve(approveDTO));
        assertEquals(BizErrorCode.AUTH_FORBIDDEN.getCode(), exception.getCode());
        assertEquals("无权操作", exception.getMessage());
    }

    @Test
    void approveShouldAllowAdminFromAnyDept() {
        Purchase purchase = new Purchase();
        purchase.setId(101L);
        purchase.setDeptId(1L);
        purchase.setStatus(PurchaseStatusEnum.PENDING_APPROVAL.getCode());
        when(purchaseMapper.selectById(101L)).thenReturn(purchase);
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L, null, RoleEnum.ADMIN.getCode()));

        PurchaseApproveDTO approveDTO = new PurchaseApproveDTO();
        approveDTO.setPurchaseId(101L);
        approveDTO.setApproverUserId(1L);
        approveDTO.setApproved(true);
        approveDTO.setRemark("管理员审批");
        purchaseService.approve(approveDTO);

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseMapper).updateById(purchaseCaptor.capture());
        assertEquals(PurchaseStatusEnum.APPROVED.getCode(), purchaseCaptor.getValue().getStatus());
        assertEquals(1L, purchaseCaptor.getValue().getApproverId());
    }

    @Test
    void confirmShouldMoveApprovedToPurchasing() {
        Purchase purchase = new Purchase();
        purchase.setId(101L);
        purchase.setStatus(PurchaseStatusEnum.APPROVED.getCode());
        when(purchaseMapper.selectById(101L)).thenReturn(purchase);
        when(userMapper.selectById(1008L)).thenReturn(buildUser(1008L, null, RoleEnum.PURCHASER.getCode()));

        PurchaseConfirmDTO confirmDTO = new PurchaseConfirmDTO();
        confirmDTO.setPurchaseId(101L);
        confirmDTO.setPurchaserUserId(1008L);
        purchaseService.confirm(confirmDTO);

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseMapper).updateById(purchaseCaptor.capture());
        assertEquals(PurchaseStatusEnum.PURCHASING.getCode(), purchaseCaptor.getValue().getStatus());
        assertEquals(1008L, purchaseCaptor.getValue().getPurchaserId());
    }

    @Test
    void confirmShouldRejectWhenOperatorNotPurchaserOrAdmin() {
        Purchase purchase = new Purchase();
        purchase.setId(101L);
        purchase.setStatus(PurchaseStatusEnum.APPROVED.getCode());
        when(purchaseMapper.selectById(101L)).thenReturn(purchase);
        when(userMapper.selectById(1008L)).thenReturn(buildUser(1008L, 1L, RoleEnum.DEPT_EMPLOYEE.getCode()));

        PurchaseConfirmDTO confirmDTO = new PurchaseConfirmDTO();
        confirmDTO.setPurchaseId(101L);
        confirmDTO.setPurchaserUserId(1008L);

        BusinessException exception = assertThrows(BusinessException.class, () -> purchaseService.confirm(confirmDTO));
        assertEquals(BizErrorCode.AUTH_FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    void cancelShouldRejectWhenInboundExists() {
        Purchase purchase = new Purchase();
        purchase.setId(101L);
        purchase.setApplyUserId(1001L);
        purchase.setStatus(PurchaseStatusEnum.PENDING_APPROVAL.getCode());
        when(purchaseMapper.selectById(101L)).thenReturn(purchase);
        when(userMapper.selectById(1001L)).thenReturn(buildUser(1001L, 1L, RoleEnum.DEPT_EMPLOYEE.getCode()));
        when(inboundMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        PurchaseCancelDTO cancelDTO = new PurchaseCancelDTO();
        cancelDTO.setPurchaseId(101L);
        cancelDTO.setOperatorUserId(1001L);

        BusinessException exception = assertThrows(BusinessException.class, () -> purchaseService.cancel(cancelDTO));
        assertEquals("采购单已存在关联入库单，不允许取消", exception.getMessage());
    }

    @Test
    void cancelShouldRejectWhenOperatorNotApplicantPurchaserOrAdmin() {
        Purchase purchase = new Purchase();
        purchase.setId(101L);
        purchase.setApplyUserId(1001L);
        purchase.setStatus(PurchaseStatusEnum.PENDING_APPROVAL.getCode());
        when(purchaseMapper.selectById(101L)).thenReturn(purchase);
        when(userMapper.selectById(1002L)).thenReturn(buildUser(1002L, 1L, RoleEnum.DEPT_EMPLOYEE.getCode()));

        PurchaseCancelDTO cancelDTO = new PurchaseCancelDTO();
        cancelDTO.setPurchaseId(101L);
        cancelDTO.setOperatorUserId(1002L);

        BusinessException exception = assertThrows(BusinessException.class, () -> purchaseService.cancel(cancelDTO));
        assertEquals(BizErrorCode.AUTH_FORBIDDEN.getCode(), exception.getCode());
    }

    private AddPurchaseDTO buildAddPurchaseDTO() {
        AddPurchaseItemDTO itemDTO = new AddPurchaseItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(100);
        itemDTO.setUnitPrice(new BigDecimal("2.20"));

        AddPurchaseDTO addPurchaseDTO = new AddPurchaseDTO();
        addPurchaseDTO.setDeptId(1L);
        addPurchaseDTO.setApplyUserId(1001L);
        addPurchaseDTO.setRequireDate(LocalDate.now().plusDays(1));
        addPurchaseDTO.setSupplierId(3L);
        addPurchaseDTO.setRemark("客房用品补充");
        addPurchaseDTO.setItems(List.of(itemDTO));
        return addPurchaseDTO;
    }

    private User buildUser(Long id, Long deptId, String role) {
        User user = new User();
        user.setId(id);
        user.setDeptId(deptId);
        user.setRole(role);
        return user;
    }
}
