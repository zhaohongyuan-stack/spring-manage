package com.fengrui.frmanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.frmanage.common.enums.InventoryChangeTypeEnum;
import com.fengrui.frmanage.common.enums.ProductStatusEnum;
import com.fengrui.frmanage.dto.inventory.InitInventoryDTO;
import com.fengrui.frmanage.dto.inventory.InitInventoryItemDTO;
import com.fengrui.frmanage.dto.inventory.InventoryQueryDTO;
import com.fengrui.frmanage.dto.inventory.InventoryRecordQueryDTO;
import com.fengrui.frmanage.entity.Inventory;
import com.fengrui.frmanage.entity.InventoryRecord;
import com.fengrui.frmanage.entity.Product;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.InventoryMapper;
import com.fengrui.frmanage.mapper.InventoryRecordMapper;
import com.fengrui.frmanage.mapper.ProductMapper;
import com.fengrui.frmanage.mapper.UserMapper;
import com.fengrui.frmanage.service.impl.inventory.InventoryServiceImpl;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.inventory.InventoryRecordVO;
import com.fengrui.frmanage.vo.inventory.InventoryVO;
import com.fengrui.frmanage.vo.inventory.InventoryWarningVO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存业务服务测试。
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryRecordMapper inventoryRecordMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryMapper, inventoryRecordMapper, productMapper, userMapper);
    }

    @Test
    void pageInventoryShouldReturnMapperPage() {
        Page<InventoryVO> mapperPage = Page.of(1, 10);
        InventoryVO inventoryVO = new InventoryVO();
        inventoryVO.setProductId(1L);
        inventoryVO.setProductName("一次性牙具套装");
        inventoryVO.setStockQuantity(0);
        mapperPage.setRecords(List.of(inventoryVO));
        mapperPage.setTotal(1L);
        when(inventoryMapper.selectInventoryPage(any(Page.class), eq("牙具"), isNull())).thenReturn(mapperPage);

        InventoryQueryDTO queryDTO = new InventoryQueryDTO();
        queryDTO.setProductName("牙具");
        PageResultVO<InventoryVO> result = inventoryService.pageInventory(queryDTO);

        assertEquals(1L, result.getTotal());
        assertEquals(0, result.getList().get(0).getStockQuantity());
    }

    @Test
    void pageWarningShouldReturnSuggestQuantity() {
        Page<InventoryWarningVO> mapperPage = Page.of(1, 10);
        InventoryWarningVO warningVO = new InventoryWarningVO();
        warningVO.setProductId(1L);
        warningVO.setStockQuantity(20);
        warningVO.setWarningThreshold(50);
        warningVO.setSuggestQuantity(80);
        mapperPage.setRecords(List.of(warningVO));
        mapperPage.setTotal(1L);
        when(inventoryMapper.selectWarningPage(any(Page.class), isNull(), isNull())).thenReturn(mapperPage);

        PageResultVO<InventoryWarningVO> result = inventoryService.pageWarning(new InventoryQueryDTO());

        assertEquals(80, result.getList().get(0).getSuggestQuantity());
    }

    @Test
    void pageRecordShouldConvertChangeTypeName() {
        Page<InventoryRecord> mapperPage = Page.of(1, 10);
        InventoryRecord record = new InventoryRecord();
        record.setId(301L);
        record.setProductId(1L);
        record.setProductName("一次性牙具套装");
        record.setChangeType(InventoryChangeTypeEnum.NORMAL_OUTBOUND.getCode().shortValue());
        record.setChangeQuantity(-50);
        mapperPage.setRecords(List.of(record));
        mapperPage.setTotal(1L);
        when(inventoryRecordMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mapperPage);

        InventoryRecordQueryDTO queryDTO = new InventoryRecordQueryDTO();
        queryDTO.setChangeType(InventoryChangeTypeEnum.NORMAL_OUTBOUND.getCode());
        PageResultVO<InventoryRecordVO> result = inventoryService.pageRecord(queryDTO);

        assertEquals("正常出库", result.getList().get(0).getChangeTypeName());
        assertEquals(-50, result.getList().get(0).getChangeQuantity());
    }

    @Test
    void pageRecordShouldRejectInvalidChangeType() {
        InventoryRecordQueryDTO queryDTO = new InventoryRecordQueryDTO();
        queryDTO.setChangeType(99);

        BusinessException exception = assertThrows(BusinessException.class, () -> inventoryService.pageRecord(queryDTO));
        assertEquals("库存变动类型不正确", exception.getMessage());
    }

    @Test
    void initInventoryShouldInsertNewInventoryAndRecord() {
        when(userMapper.selectById(1L)).thenReturn(buildUser());
        when(productMapper.selectById(1L)).thenReturn(buildProduct());
        when(inventoryRecordMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(inventoryMapper.selectById(1L)).thenReturn(null);

        inventoryService.initInventory(buildInitDTO(100, "2.50"));

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        ArgumentCaptor<InventoryRecord> recordCaptor = ArgumentCaptor.forClass(InventoryRecord.class);
        verify(inventoryMapper).insert(inventoryCaptor.capture());
        verify(inventoryRecordMapper).insert(recordCaptor.capture());
        assertEquals(100, inventoryCaptor.getValue().getStockQuantity());
        assertEquals(new BigDecimal("2.50"), inventoryCaptor.getValue().getUnitCost());
        assertEquals(new BigDecimal("250.00"), inventoryCaptor.getValue().getTotalCost());
        assertEquals(InventoryChangeTypeEnum.INITIAL_STOCK.getCode().shortValue(), recordCaptor.getValue().getChangeType());
        assertEquals(0, recordCaptor.getValue().getBeforeQuantity());
        assertEquals(100, recordCaptor.getValue().getAfterQuantity());
    }

    @Test
    void initInventoryShouldUpdateExistingInventoryAndCreateDeltaRecord() {
        when(userMapper.selectById(1L)).thenReturn(buildUser());
        when(productMapper.selectById(1L)).thenReturn(buildProduct());
        when(inventoryRecordMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(inventoryMapper.selectById(1L)).thenReturn(buildInventory(60, "2.00", "120.00"));

        inventoryService.initInventory(buildInitDTO(100, "2.50"));

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        ArgumentCaptor<InventoryRecord> recordCaptor = ArgumentCaptor.forClass(InventoryRecord.class);
        verify(inventoryMapper).updateById(inventoryCaptor.capture());
        verify(inventoryRecordMapper).insert(recordCaptor.capture());
        assertEquals(100, inventoryCaptor.getValue().getStockQuantity());
        assertEquals(new BigDecimal("250.00"), inventoryCaptor.getValue().getTotalCost());
        assertEquals(40, recordCaptor.getValue().getChangeQuantity());
        assertEquals("覆盖初始化", recordCaptor.getValue().getRemark());
    }

    @Test
    void initInventoryShouldSkipWhenQuantityAndUnitCostSame() {
        when(userMapper.selectById(1L)).thenReturn(buildUser());
        when(productMapper.selectById(1L)).thenReturn(buildProduct());
        when(inventoryRecordMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(inventoryMapper.selectById(1L)).thenReturn(buildInventory(100, "2.50", "250.00"));

        inventoryService.initInventory(buildInitDTO(100, "2.50"));

        verify(inventoryMapper, never()).updateById(any(Inventory.class));
        verify(inventoryRecordMapper, never()).insert(any(InventoryRecord.class));
    }

    @Test
    void initInventoryShouldRejectWhenBusinessRecordExists() {
        when(userMapper.selectById(1L)).thenReturn(buildUser());
        when(productMapper.selectById(1L)).thenReturn(buildProduct());
        when(inventoryRecordMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> inventoryService.initInventory(buildInitDTO(100, "2.50")));
        assertEquals("商品【一次性牙具套装】已存在业务库存流水，不允许初始化覆盖", exception.getMessage());
    }

    private InitInventoryDTO buildInitDTO(Integer initialQuantity, String unitCost) {
        InitInventoryItemDTO itemDTO = new InitInventoryItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setInitialQuantity(initialQuantity);
        itemDTO.setUnitCost(new BigDecimal(unitCost));

        InitInventoryDTO initDTO = new InitInventoryDTO();
        initDTO.setOperatorUserId(1L);
        initDTO.setItems(List.of(itemDTO));
        return initDTO;
    }

    private Product buildProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setProductName("一次性牙具套装");
        product.setStatus(ProductStatusEnum.ENABLED.getCode().shortValue());
        return product;
    }

    private Inventory buildInventory(Integer stockQuantity, String unitCost, String totalCost) {
        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setStockQuantity(stockQuantity);
        inventory.setUnitCost(new BigDecimal(unitCost));
        inventory.setTotalCost(new BigDecimal(totalCost));
        return inventory;
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setRealName("管理员");
        return user;
    }
}
