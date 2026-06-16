package com.fengrui.frmanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fengrui.frmanage.common.enums.InboundStatusEnum;
import com.fengrui.frmanage.common.enums.InboundTypeEnum;
import com.fengrui.frmanage.common.enums.PurchaseStatusEnum;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.dto.inbound.AddInboundDTO;
import com.fengrui.frmanage.dto.inbound.AddInboundItemDTO;
import com.fengrui.frmanage.dto.inbound.InboundConfirmDTO;
import com.fengrui.frmanage.entity.Inbound;
import com.fengrui.frmanage.entity.InboundItem;
import com.fengrui.frmanage.entity.Inventory;
import com.fengrui.frmanage.entity.InventoryRecord;
import com.fengrui.frmanage.entity.Purchase;
import com.fengrui.frmanage.entity.PurchaseItem;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.InboundItemMapper;
import com.fengrui.frmanage.mapper.InboundMapper;
import com.fengrui.frmanage.mapper.InventoryMapper;
import com.fengrui.frmanage.mapper.InventoryRecordMapper;
import com.fengrui.frmanage.mapper.PurchaseItemMapper;
import com.fengrui.frmanage.mapper.PurchaseMapper;
import com.fengrui.frmanage.mapper.SupplierMapper;
import com.fengrui.frmanage.mapper.UserMapper;
import com.fengrui.frmanage.service.impl.inbound.InboundServiceImpl;
import com.fengrui.frmanage.service.inbound.InboundNoService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 入库业务服务测试。
 */
@ExtendWith(MockitoExtension.class)
class InboundServiceImplTest {

    @Mock
    private InboundMapper inboundMapper;

    @Mock
    private InboundItemMapper inboundItemMapper;

    @Mock
    private PurchaseMapper purchaseMapper;

    @Mock
    private PurchaseItemMapper purchaseItemMapper;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryRecordMapper inventoryRecordMapper;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private InboundNoService inboundNoService;

    private InboundServiceImpl inboundService;

    @BeforeEach
    void setUp() {
        inboundService = new InboundServiceImpl(
                inboundMapper,
                inboundItemMapper,
                purchaseMapper,
                purchaseItemMapper,
                inventoryMapper,
                inventoryRecordMapper,
                supplierMapper,
                userMapper,
                inboundNoService
        );
    }

    @Test
    void addInboundShouldInsertHeaderAndItems() {
        when(purchaseMapper.selectById(101L)).thenReturn(buildPurchase(PurchaseStatusEnum.APPROVED.getCode()));
        when(userMapper.selectById(2001L)).thenReturn(new User());
        when(purchaseItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(buildPurchaseItem(1L, 100, 20)));
        when(inboundNoService.generateInboundNo()).thenReturn("RK-20260616-001");
        when(inboundMapper.insert(any(Inbound.class))).thenAnswer(invocation -> {
            Inbound inbound = invocation.getArgument(0);
            inbound.setId(301L);
            return 1;
        });

        inboundService.addInbound(buildAddInboundDTO(80));

        ArgumentCaptor<Inbound> inboundCaptor = ArgumentCaptor.forClass(Inbound.class);
        ArgumentCaptor<InboundItem> itemCaptor = ArgumentCaptor.forClass(InboundItem.class);
        verify(inboundMapper).insert(inboundCaptor.capture());
        verify(inboundItemMapper).insert(itemCaptor.capture());
        assertEquals("RK-20260616-001", inboundCaptor.getValue().getInboundNo());
        assertEquals(new BigDecimal("176.00"), inboundCaptor.getValue().getTotalAmount());
        assertEquals(301L, itemCaptor.getValue().getInboundId());
        assertEquals(80, itemCaptor.getValue().getActualQuantity());
    }

    @Test
    void addInboundShouldRejectWhenQuantityExceedsRemaining() {
        when(purchaseMapper.selectById(101L)).thenReturn(buildPurchase(PurchaseStatusEnum.APPROVED.getCode()));
        when(userMapper.selectById(2001L)).thenReturn(new User());
        when(purchaseItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(buildPurchaseItem(1L, 100, 20)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.addInbound(buildAddInboundDTO(81)));

        assertEquals("商品【一次性牙具套装】入库数量超过剩余可入数量", exception.getMessage());
    }

    @Test
    void confirmNormalInboundShouldUpdateInventoryAndPurchaseStatusWhenFullyReceived() {
        when(inboundMapper.selectById(301L)).thenReturn(buildInbound(InboundTypeEnum.NORMAL));
        when(purchaseMapper.selectById(101L)).thenReturn(buildPurchase(PurchaseStatusEnum.PURCHASING.getCode()));
        when(userMapper.selectById(1005L)).thenReturn(buildDeptHead());
        when(userMapper.selectById(2001L)).thenReturn(buildWarehouseKeeper());
        InboundItem inboundItem = buildInboundItem(80);
        when(inboundItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(inboundItem));
        PurchaseItem purchaseItem = buildPurchaseItem(1L, 100, 20);
        when(purchaseItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(purchaseItem));
        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setStockQuantity(20);
        inventory.setUnitCost(new BigDecimal("2.00"));
        inventory.setTotalCost(new BigDecimal("40.00"));
        when(inventoryMapper.selectById(1L)).thenReturn(inventory);

        inboundService.confirm(buildConfirmDTO());

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        ArgumentCaptor<InventoryRecord> recordCaptor = ArgumentCaptor.forClass(InventoryRecord.class);
        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(inventoryMapper).updateById(inventoryCaptor.capture());
        verify(inventoryRecordMapper).insert(recordCaptor.capture());
        verify(purchaseMapper).updateById(purchaseCaptor.capture());
        assertEquals(100, inventoryCaptor.getValue().getStockQuantity());
        assertEquals(new BigDecimal("216.00"), inventoryCaptor.getValue().getTotalCost());
        assertEquals(new BigDecimal("2.16"), inventoryCaptor.getValue().getUnitCost());
        assertEquals(20, recordCaptor.getValue().getBeforeQuantity());
        assertEquals(100, recordCaptor.getValue().getAfterQuantity());
        assertEquals(PurchaseStatusEnum.INBOUNDED.getCode(), purchaseCaptor.getValue().getStatus());
    }

    @Test
    void confirmDirectFoodShouldNotUpdateInventory() {
        when(inboundMapper.selectById(301L)).thenReturn(buildInbound(InboundTypeEnum.DIRECT_FOOD));
        when(purchaseMapper.selectById(101L)).thenReturn(buildPurchase(PurchaseStatusEnum.PURCHASING.getCode()));
        when(userMapper.selectById(1005L)).thenReturn(buildDeptHead());
        when(userMapper.selectById(2001L)).thenReturn(buildWarehouseKeeper());
        when(inboundItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(buildInboundItem(80)));
        when(purchaseItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(buildPurchaseItem(1L, 100, 20)));
        Inventory inventory = new Inventory();
        inventory.setStockQuantity(20);
        when(inventoryMapper.selectById(1L)).thenReturn(inventory);

        inboundService.confirm(buildConfirmDTO());

        ArgumentCaptor<InventoryRecord> recordCaptor = ArgumentCaptor.forClass(InventoryRecord.class);
        verify(inventoryMapper, never()).updateById(any(Inventory.class));
        verify(inventoryRecordMapper).insert(recordCaptor.capture());
        assertEquals(-80, recordCaptor.getValue().getChangeQuantity());
        assertEquals(20, recordCaptor.getValue().getBeforeQuantity());
        assertEquals(20, recordCaptor.getValue().getAfterQuantity());
    }

    @Test
    void confirmShouldValidateDeptHead() {
        when(inboundMapper.selectById(301L)).thenReturn(buildInbound(InboundTypeEnum.NORMAL));
        when(purchaseMapper.selectById(101L)).thenReturn(buildPurchase(PurchaseStatusEnum.PURCHASING.getCode()));
        User user = new User();
        user.setId(1005L);
        user.setRole(RoleEnum.PURCHASER.getCode());
        user.setDeptId(1L);
        when(userMapper.selectById(1005L)).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> inboundService.confirm(buildConfirmDTO()));

        assertEquals("验收人必须是申请部门负责人", exception.getMessage());
    }

    private AddInboundDTO buildAddInboundDTO(Integer actualQuantity) {
        AddInboundItemDTO itemDTO = new AddInboundItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setActualQuantity(actualQuantity);
        AddInboundDTO addInboundDTO = new AddInboundDTO();
        addInboundDTO.setPurchaseId(101L);
        addInboundDTO.setReceiverUserId(2001L);
        addInboundDTO.setInboundType(InboundTypeEnum.NORMAL.getCode());
        addInboundDTO.setRemark("入库测试");
        addInboundDTO.setItems(List.of(itemDTO));
        return addInboundDTO;
    }

    private InboundConfirmDTO buildConfirmDTO() {
        InboundConfirmDTO confirmDTO = new InboundConfirmDTO();
        confirmDTO.setInboundId(301L);
        confirmDTO.setDeptHeadUserId(1005L);
        confirmDTO.setWarehouseKeeperUserId(2001L);
        return confirmDTO;
    }

    private Purchase buildPurchase(Integer status) {
        Purchase purchase = new Purchase();
        purchase.setId(101L);
        purchase.setPurchaseNo("CG-20260616-001");
        purchase.setDeptId(1L);
        purchase.setSupplierId(3L);
        purchase.setStatus(status);
        return purchase;
    }

    private PurchaseItem buildPurchaseItem(Long productId, Integer quantity, Integer receivedQuantity) {
        PurchaseItem purchaseItem = new PurchaseItem();
        purchaseItem.setId(501L);
        purchaseItem.setProductId(productId);
        purchaseItem.setProductName("一次性牙具套装");
        purchaseItem.setQuantity(quantity);
        purchaseItem.setUnitPrice(new BigDecimal("2.20"));
        purchaseItem.setReceivedQuantity(receivedQuantity);
        return purchaseItem;
    }

    private Inbound buildInbound(InboundTypeEnum inboundType) {
        Inbound inbound = new Inbound();
        inbound.setId(301L);
        inbound.setInboundNo("RK-20260616-001");
        inbound.setPurchaseId(101L);
        inbound.setInboundType(inboundType.getCode().shortValue());
        inbound.setStatus(InboundStatusEnum.PENDING_ACCEPTANCE.getCode().shortValue());
        inbound.setRemark("入库测试");
        return inbound;
    }

    private InboundItem buildInboundItem(Integer actualQuantity) {
        InboundItem inboundItem = new InboundItem();
        inboundItem.setProductId(1L);
        inboundItem.setProductName("一次性牙具套装");
        inboundItem.setActualQuantity(actualQuantity);
        inboundItem.setAmount(new BigDecimal("176.00"));
        return inboundItem;
    }

    private User buildDeptHead() {
        User user = new User();
        user.setId(1005L);
        user.setRole(RoleEnum.DEPT_HEAD.getCode());
        user.setDeptId(1L);
        user.setRealName("李经理");
        return user;
    }

    private User buildWarehouseKeeper() {
        User user = new User();
        user.setId(2001L);
        user.setRealName("李四");
        return user;
    }
}
