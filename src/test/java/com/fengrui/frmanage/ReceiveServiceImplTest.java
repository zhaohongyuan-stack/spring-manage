package com.fengrui.frmanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fengrui.frmanage.common.enums.InventoryChangeTypeEnum;
import com.fengrui.frmanage.common.enums.ProductStatusEnum;
import com.fengrui.frmanage.common.enums.ReceiveStatusEnum;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.dto.receive.AddReceiveDTO;
import com.fengrui.frmanage.dto.receive.AddReceiveItemDTO;
import com.fengrui.frmanage.dto.receive.ReceiveApproveDTO;
import com.fengrui.frmanage.dto.receive.ReceiveCancelDTO;
import com.fengrui.frmanage.dto.receive.ReceiveConfirmDTO;
import com.fengrui.frmanage.entity.Department;
import com.fengrui.frmanage.entity.Inventory;
import com.fengrui.frmanage.entity.InventoryRecord;
import com.fengrui.frmanage.entity.Product;
import com.fengrui.frmanage.entity.Receive;
import com.fengrui.frmanage.entity.ReceiveItem;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.DepartmentMapper;
import com.fengrui.frmanage.mapper.InventoryMapper;
import com.fengrui.frmanage.mapper.InventoryRecordMapper;
import com.fengrui.frmanage.mapper.ProductMapper;
import com.fengrui.frmanage.mapper.ReceiveItemMapper;
import com.fengrui.frmanage.mapper.ReceiveMapper;
import com.fengrui.frmanage.mapper.UserMapper;
import com.fengrui.frmanage.service.impl.receive.ReceiveServiceImpl;
import com.fengrui.frmanage.service.receive.ReceiveNoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 领用出库业务服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ReceiveServiceImplTest {

    @Mock
    private ReceiveMapper receiveMapper;

    @Mock
    private ReceiveItemMapper receiveItemMapper;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryRecordMapper inventoryRecordMapper;

    @Mock
    private ReceiveNoService receiveNoService;

    private ReceiveServiceImpl receiveService;

    @BeforeEach
    void setUp() {
        receiveService = new ReceiveServiceImpl(
                receiveMapper,
                receiveItemMapper,
                departmentMapper,
                userMapper,
                productMapper,
                inventoryMapper,
                inventoryRecordMapper,
                receiveNoService
        );
    }

    @Test
    void addReceiveShouldInsertHeaderAndItemsWithUnitCostSnapshot() {
        when(departmentMapper.selectById(1L)).thenReturn(new Department());
        User applyUser = buildUser(1001L, 1L, RoleEnum.DEPT_EMPLOYEE.getCode(), "张三");
        when(userMapper.selectById(1001L)).thenReturn(applyUser);
        when(receiveNoService.generateReceiveNo()).thenReturn("LY-20260616-001");
        when(productMapper.selectById(1L)).thenReturn(buildProduct(1L, "一次性牙具套装"));
        when(inventoryMapper.selectById(1L)).thenReturn(buildInventory(1L, 100, "2.20", "220.00"));
        when(receiveMapper.insert(any(Receive.class))).thenAnswer(invocation -> {
            Receive receive = invocation.getArgument(0);
            receive.setId(201L);
            return 1;
        });

        receiveService.addReceive(buildAddReceiveDTO());

        ArgumentCaptor<Receive> receiveCaptor = ArgumentCaptor.forClass(Receive.class);
        ArgumentCaptor<ReceiveItem> itemCaptor = ArgumentCaptor.forClass(ReceiveItem.class);
        verify(receiveMapper).insert(receiveCaptor.capture());
        verify(receiveItemMapper).insert(itemCaptor.capture());
        assertEquals("LY-20260616-001", receiveCaptor.getValue().getReceiveNo());
        assertEquals(new BigDecimal("110.00"), receiveCaptor.getValue().getTotalAmount());
        assertEquals(ReceiveStatusEnum.PENDING_APPROVAL.getCode().shortValue(), receiveCaptor.getValue().getStatus());
        assertEquals(201L, itemCaptor.getValue().getReceiveId());
        assertEquals(new BigDecimal("2.20"), itemCaptor.getValue().getUnitPrice());
        assertEquals(new BigDecimal("110.00"), itemCaptor.getValue().getAmount());
    }

    @Test
    void addReceiveShouldRejectWhenApplyUserDeptMismatch() {
        when(departmentMapper.selectById(1L)).thenReturn(new Department());
        when(userMapper.selectById(1001L)).thenReturn(buildUser(1001L, 2L, RoleEnum.DEPT_EMPLOYEE.getCode(), "张三"));

        BusinessException exception = assertThrows(BusinessException.class, () -> receiveService.addReceive(buildAddReceiveDTO()));
        assertEquals("申请人所属部门必须与领用部门一致", exception.getMessage());
    }

    @Test
    void approveShouldRequireRemarkWhenRejected() {
        Receive receive = buildReceive(201L, ReceiveStatusEnum.PENDING_APPROVAL);
        when(receiveMapper.selectById(201L)).thenReturn(receive);
        when(userMapper.selectById(1005L)).thenReturn(buildUser(1005L, 1L, RoleEnum.DEPT_HEAD.getCode(), "李经理"));

        ReceiveApproveDTO approveDTO = new ReceiveApproveDTO();
        approveDTO.setReceiveId(201L);
        approveDTO.setApproverUserId(1005L);
        approveDTO.setApproved(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> receiveService.approve(approveDTO));
        assertEquals("驳回时必须填写驳回理由", exception.getMessage());
    }

    @Test
    void approveShouldMovePendingToPendingOutbound() {
        Receive receive = buildReceive(201L, ReceiveStatusEnum.PENDING_APPROVAL);
        when(receiveMapper.selectById(201L)).thenReturn(receive);
        when(userMapper.selectById(1005L)).thenReturn(buildUser(1005L, 1L, RoleEnum.DEPT_HEAD.getCode(), "李经理"));

        ReceiveApproveDTO approveDTO = new ReceiveApproveDTO();
        approveDTO.setReceiveId(201L);
        approveDTO.setApproverUserId(1005L);
        approveDTO.setApproved(true);
        receiveService.approve(approveDTO);

        ArgumentCaptor<Receive> receiveCaptor = ArgumentCaptor.forClass(Receive.class);
        verify(receiveMapper).updateById(receiveCaptor.capture());
        assertEquals(ReceiveStatusEnum.PENDING_OUTBOUND.getCode().shortValue(), receiveCaptor.getValue().getStatus());
        assertEquals(1005L, receiveCaptor.getValue().getApproverId());
    }

    @Test
    void confirmShouldRejectWhenStockInsufficient() {
        Receive receive = buildReceive(201L, ReceiveStatusEnum.PENDING_OUTBOUND);
        when(receiveMapper.selectById(201L)).thenReturn(receive);
        when(userMapper.selectById(1010L)).thenReturn(buildUser(1010L, 1L, RoleEnum.WAREHOUSE_KEEPER.getCode(), "王五"));
        when(receiveItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(buildReceiveItem(1L, 50, "2.20")));
        when(inventoryMapper.selectById(1L)).thenReturn(buildInventory(1L, 10, "2.20", "22.00"));

        BusinessException exception = assertThrows(BusinessException.class, () -> receiveService.confirm(buildConfirmDTO()));
        assertEquals("商品【一次性牙具套装】库存不足", exception.getMessage());
    }

    @Test
    void confirmShouldRejectWhenDelivererNotWarehouseKeeperOrAdmin() {
        Receive receive = buildReceive(201L, ReceiveStatusEnum.PENDING_OUTBOUND);
        when(receiveMapper.selectById(201L)).thenReturn(receive);
        when(userMapper.selectById(1010L)).thenReturn(buildUser(1010L, 1L, RoleEnum.DEPT_EMPLOYEE.getCode(), "王五"));

        BusinessException exception = assertThrows(BusinessException.class, () -> receiveService.confirm(buildConfirmDTO()));
        assertEquals("无权操作", exception.getMessage());
    }

    @Test
    void confirmShouldDeductInventoryAndCreateRecord() {
        Receive receive = buildReceive(201L, ReceiveStatusEnum.PENDING_OUTBOUND);
        when(receiveMapper.selectById(201L)).thenReturn(receive);
        when(userMapper.selectById(1010L)).thenReturn(buildUser(1010L, 1L, RoleEnum.WAREHOUSE_KEEPER.getCode(), "王五"));
        when(receiveItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(buildReceiveItem(1L, 50, "2.20")));
        when(inventoryMapper.selectById(1L)).thenReturn(buildInventory(1L, 100, "2.20", "220.00"));

        receiveService.confirm(buildConfirmDTO());

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        ArgumentCaptor<InventoryRecord> recordCaptor = ArgumentCaptor.forClass(InventoryRecord.class);
        ArgumentCaptor<Receive> receiveCaptor = ArgumentCaptor.forClass(Receive.class);
        verify(inventoryMapper).updateById(inventoryCaptor.capture());
        verify(inventoryRecordMapper).insert(recordCaptor.capture());
        verify(receiveMapper).updateById(receiveCaptor.capture());
        assertEquals(50, inventoryCaptor.getValue().getStockQuantity());
        assertEquals(new BigDecimal("110.00"), inventoryCaptor.getValue().getTotalCost());
        assertEquals(InventoryChangeTypeEnum.NORMAL_OUTBOUND.getCode().shortValue(), recordCaptor.getValue().getChangeType());
        assertEquals(-50, recordCaptor.getValue().getChangeQuantity());
        assertEquals(ReceiveStatusEnum.OUTBOUNDED.getCode().shortValue(), receiveCaptor.getValue().getStatus());
    }

    @Test
    void cancelShouldRejectWhenOutbounded() {
        Receive receive = buildReceive(201L, ReceiveStatusEnum.OUTBOUNDED);
        when(receiveMapper.selectById(201L)).thenReturn(receive);
        when(userMapper.selectById(1001L)).thenReturn(buildUser(1001L, 1L, RoleEnum.DEPT_EMPLOYEE.getCode(), "张三"));

        ReceiveCancelDTO cancelDTO = new ReceiveCancelDTO();
        cancelDTO.setReceiveId(201L);
        cancelDTO.setOperatorUserId(1001L);

        BusinessException exception = assertThrows(BusinessException.class, () -> receiveService.cancel(cancelDTO));
        assertEquals("当前领用单状态不可取消", exception.getMessage());
    }

    @Test
    void cancelShouldRejectWhenOperatorNotApplicantOrAdmin() {
        Receive receive = buildReceive(201L, ReceiveStatusEnum.PENDING_APPROVAL);
        when(receiveMapper.selectById(201L)).thenReturn(receive);
        when(userMapper.selectById(1002L)).thenReturn(buildUser(1002L, 1L, RoleEnum.DEPT_EMPLOYEE.getCode(), "李四"));

        ReceiveCancelDTO cancelDTO = new ReceiveCancelDTO();
        cancelDTO.setReceiveId(201L);
        cancelDTO.setOperatorUserId(1002L);

        BusinessException exception = assertThrows(BusinessException.class, () -> receiveService.cancel(cancelDTO));
        assertEquals("无权操作", exception.getMessage());
    }

    @Test
    void cancelShouldAllowAdmin() {
        Receive receive = buildReceive(201L, ReceiveStatusEnum.PENDING_APPROVAL);
        when(receiveMapper.selectById(201L)).thenReturn(receive);
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L, null, RoleEnum.ADMIN.getCode(), "管理员"));

        ReceiveCancelDTO cancelDTO = new ReceiveCancelDTO();
        cancelDTO.setReceiveId(201L);
        cancelDTO.setOperatorUserId(1L);
        receiveService.cancel(cancelDTO);

        ArgumentCaptor<Receive> receiveCaptor = ArgumentCaptor.forClass(Receive.class);
        verify(receiveMapper).updateById(receiveCaptor.capture());
        assertEquals(ReceiveStatusEnum.CANCELLED.getCode().shortValue(), receiveCaptor.getValue().getStatus());
    }

    private AddReceiveDTO buildAddReceiveDTO() {
        AddReceiveItemDTO itemDTO = new AddReceiveItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(50);

        AddReceiveDTO addReceiveDTO = new AddReceiveDTO();
        addReceiveDTO.setDeptId(1L);
        addReceiveDTO.setApplyUserId(1001L);
        addReceiveDTO.setPurpose("客房用品补充");
        addReceiveDTO.setRemark("急需");
        addReceiveDTO.setItems(List.of(itemDTO));
        return addReceiveDTO;
    }

    private ReceiveConfirmDTO buildConfirmDTO() {
        ReceiveConfirmDTO confirmDTO = new ReceiveConfirmDTO();
        confirmDTO.setReceiveId(201L);
        confirmDTO.setDelivererUserId(1010L);
        confirmDTO.setRemark("已全部发货");
        return confirmDTO;
    }

    private Receive buildReceive(Long id, ReceiveStatusEnum statusEnum) {
        Receive receive = new Receive();
        receive.setId(id);
        receive.setReceiveNo("LY-20260616-001");
        receive.setDeptId(1L);
        receive.setApplyUserId(1001L);
        receive.setStatus(statusEnum.getCode().shortValue());
        receive.setRemark("急需");
        return receive;
    }

    private ReceiveItem buildReceiveItem(Long productId, Integer quantity, String unitPrice) {
        ReceiveItem item = new ReceiveItem();
        item.setReceiveId(201L);
        item.setProductId(productId);
        item.setProductName("一次性牙具套装");
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        item.setAmount(new BigDecimal(unitPrice).multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    private Product buildProduct(Long id, String productName) {
        Product product = new Product();
        product.setId(id);
        product.setProductName(productName);
        product.setStatus(ProductStatusEnum.ENABLED.getCode().shortValue());
        return product;
    }

    private Inventory buildInventory(Long productId, Integer stockQuantity, String unitCost, String totalCost) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setStockQuantity(stockQuantity);
        inventory.setUnitCost(new BigDecimal(unitCost));
        inventory.setTotalCost(new BigDecimal(totalCost));
        return inventory;
    }

    private User buildUser(Long id, Long deptId, String role, String realName) {
        User user = new User();
        user.setId(id);
        user.setDeptId(deptId);
        user.setRole(role);
        user.setRealName(realName);
        return user;
    }
}
