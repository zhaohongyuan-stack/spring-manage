package com.fengrui.frmanage.service.impl.inbound;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.frmanage.common.enums.InboundStatusEnum;
import com.fengrui.frmanage.common.enums.InboundTypeEnum;
import com.fengrui.frmanage.common.enums.InventoryChangeTypeEnum;
import com.fengrui.frmanage.common.enums.PurchaseStatusEnum;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.common.util.RoleAuthSupport;
import com.fengrui.frmanage.dto.inbound.AddInboundDTO;
import com.fengrui.frmanage.dto.inbound.AddInboundItemDTO;
import com.fengrui.frmanage.dto.inbound.InboundConfirmDTO;
import com.fengrui.frmanage.dto.inbound.InboundListQueryDTO;
import com.fengrui.frmanage.entity.Inbound;
import com.fengrui.frmanage.entity.InboundItem;
import com.fengrui.frmanage.entity.Inventory;
import com.fengrui.frmanage.entity.InventoryRecord;
import com.fengrui.frmanage.entity.Purchase;
import com.fengrui.frmanage.entity.PurchaseItem;
import com.fengrui.frmanage.entity.Supplier;
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
import com.fengrui.frmanage.service.inbound.InboundNoService;
import com.fengrui.frmanage.service.inbound.InboundService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.inbound.InboundDetailVO;
import com.fengrui.frmanage.vo.inbound.InboundItemVO;
import com.fengrui.frmanage.vo.inbound.InboundListItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 入库业务服务实现。
 */
@Service
public class InboundServiceImpl implements InboundService {

    private static final int MONEY_SCALE = 2;

    private final InboundMapper inboundMapper;

    private final InboundItemMapper inboundItemMapper;

    private final PurchaseMapper purchaseMapper;

    private final PurchaseItemMapper purchaseItemMapper;

    private final InventoryMapper inventoryMapper;

    private final InventoryRecordMapper inventoryRecordMapper;

    private final SupplierMapper supplierMapper;

    private final UserMapper userMapper;

    private final InboundNoService inboundNoService;

    public InboundServiceImpl(InboundMapper inboundMapper,
                              InboundItemMapper inboundItemMapper,
                              PurchaseMapper purchaseMapper,
                              PurchaseItemMapper purchaseItemMapper,
                              InventoryMapper inventoryMapper,
                              InventoryRecordMapper inventoryRecordMapper,
                              SupplierMapper supplierMapper,
                              UserMapper userMapper,
                              InboundNoService inboundNoService) {
        this.inboundMapper = inboundMapper;
        this.inboundItemMapper = inboundItemMapper;
        this.purchaseMapper = purchaseMapper;
        this.purchaseItemMapper = purchaseItemMapper;
        this.inventoryMapper = inventoryMapper;
        this.inventoryRecordMapper = inventoryRecordMapper;
        this.supplierMapper = supplierMapper;
        this.userMapper = userMapper;
        this.inboundNoService = inboundNoService;
    }

    /**
     * 新增入库单。
     *
     * @param addInboundDTO 新增入库单参数
     * @return 入库单ID和单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addInbound(AddInboundDTO addInboundDTO) {
        Purchase purchase = getExistingPurchase(addInboundDTO.getPurchaseId());
        validatePurchaseStatusForInbound(purchase);
        User receiver = assertUserExists(addInboundDTO.getReceiverUserId(), "仓库管理员不存在");
        RoleAuthSupport.assertAnyRole(receiver, RoleEnum.WAREHOUSE_KEEPER, RoleEnum.ADMIN);
        validateInboundType(addInboundDTO.getInboundType());

        List<PurchaseItem> purchaseItems = getPurchaseItems(purchase.getId());
        Map<Long, PurchaseItem> purchaseItemMap = buildPurchaseItemMap(purchaseItems);
        List<InboundItem> inboundItems = buildInboundItems(addInboundDTO.getItems(), purchaseItemMap);
        BigDecimal totalAmount = inboundItems.stream()
                .map(InboundItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        String inboundNo = inboundNoService.generateInboundNo();
        Inbound inbound = new Inbound();
        inbound.setInboundNo(inboundNo);
        inbound.setPurchaseId(purchase.getId());
        inbound.setSupplierId(purchase.getSupplierId());
        inbound.setReceiverId(addInboundDTO.getReceiverUserId());
        inbound.setInboundType(addInboundDTO.getInboundType().shortValue());
        inbound.setTotalAmount(totalAmount);
        inbound.setStatus(InboundStatusEnum.PENDING_ACCEPTANCE.getCode().shortValue());
        inbound.setRemark(addInboundDTO.getRemark());
        inboundMapper.insert(inbound);

        for (InboundItem inboundItem : inboundItems) {
            inboundItem.setInboundId(inbound.getId());
            inboundItemMapper.insert(inboundItem);
        }

        markPurchaseAsPurchasingIfApproved(purchase);

        Map<String, Object> result = new HashMap<>();
        result.put("inboundId", inbound.getId());
        result.put("inboundNo", inboundNo);
        return result;
    }

    /**
     * 分页查询入库单。
     *
     * @param queryDTO 查询参数
     * @return 入库单分页列表
     */
    @Override
    public PageResultVO<InboundListItemVO> pageList(InboundListQueryDTO queryDTO) {
        LambdaQueryWrapper<Inbound> queryWrapper = new LambdaQueryWrapper<Inbound>()
                .eq(queryDTO.getInboundType() != null, Inbound::getInboundType, toShort(queryDTO.getInboundType()))
                .eq(queryDTO.getStatus() != null, Inbound::getStatus, toShort(queryDTO.getStatus()))
                .orderByDesc(Inbound::getId);
        if (StringUtils.hasText(queryDTO.getPurchaseNo())) {
            List<Long> purchaseIds = purchaseMapper.selectList(
                            new LambdaQueryWrapper<Purchase>()
                                    .like(Purchase::getPurchaseNo, queryDTO.getPurchaseNo())
                    )
                    .stream()
                    .map(Purchase::getId)
                    .toList();
            if (purchaseIds.isEmpty()) {
                return new PageResultVO<>(0L, queryDTO.getPageNum(), queryDTO.getPageSize(), List.of());
            }
            queryWrapper.in(Inbound::getPurchaseId, purchaseIds);
        }
        IPage<Inbound> inboundPage = inboundMapper.selectPage(
                Page.of(queryDTO.getPageNum(), queryDTO.getPageSize()),
                queryWrapper
        );
        List<InboundListItemVO> records = inboundPage.getRecords()
                .stream()
                .map(this::toListItemVO)
                .toList();
        return new PageResultVO<>(inboundPage.getTotal(), inboundPage.getCurrent(), inboundPage.getSize(), records);
    }

    /**
     * 查询入库单详情。
     *
     * @param inboundId 入库单ID
     * @return 入库单详情
     */
    @Override
    public InboundDetailVO detail(Long inboundId) {
        Inbound inbound = getExistingInbound(inboundId);
        List<InboundItem> items = inboundItemMapper.selectList(
                new LambdaQueryWrapper<InboundItem>().eq(InboundItem::getInboundId, inboundId)
        );
        InboundDetailVO detailVO = toDetailVO(inbound);
        detailVO.setItems(items.stream().map(this::toItemVO).toList());
        return detailVO;
    }

    /**
     * 验收入库单。
     *
     * @param confirmDTO 验收参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(InboundConfirmDTO confirmDTO) {
        Inbound inbound = getExistingInbound(confirmDTO.getInboundId());
        if (!toShort(InboundStatusEnum.PENDING_ACCEPTANCE.getCode()).equals(inbound.getStatus())) {
            throw new BusinessException("只有待验收入库单可以验收");
        }
        Purchase purchase = getExistingPurchase(inbound.getPurchaseId());
        validateDeptHead(confirmDTO.getDeptHeadUserId(), purchase);
        User warehouseKeeper = assertUserExists(confirmDTO.getWarehouseKeeperUserId(), "仓库管理员不存在");
        RoleAuthSupport.assertAnyRole(warehouseKeeper, RoleEnum.WAREHOUSE_KEEPER, RoleEnum.ADMIN);
        List<InboundItem> inboundItems = inboundItemMapper.selectList(
                new LambdaQueryWrapper<InboundItem>().eq(InboundItem::getInboundId, inbound.getId())
        );
        List<PurchaseItem> purchaseItems = getPurchaseItems(purchase.getId());
        Map<Long, PurchaseItem> purchaseItemMap = buildPurchaseItemMap(purchaseItems);

        // TODO 后续库存并发更新建议使用 SELECT ... FOR UPDATE 或 inventory.version 乐观锁。
        for (InboundItem inboundItem : inboundItems) {
            if (toShort(InboundTypeEnum.NORMAL.getCode()).equals(inbound.getInboundType())) {
                handleNormalInbound(inbound, inboundItem, warehouseKeeper);
            } else {
                handleDirectFoodInbound(inbound, inboundItem, warehouseKeeper);
            }
            updateReceivedQuantity(inboundItem, purchaseItemMap);
        }

        Inbound updateInbound = new Inbound();
        updateInbound.setId(inbound.getId());
        updateInbound.setStatus(InboundStatusEnum.ACCEPTED.getCode().shortValue());
        updateInbound.setDeptHeadId(confirmDTO.getDeptHeadUserId());
        updateInbound.setAcceptTime(LocalDateTime.now());
        inboundMapper.updateById(updateInbound);

        updatePurchaseStatusAfterAcceptance(purchase.getId());
    }

    /**
     * 部门负责人验收完成后同步采购单状态。
     * 验收通过即代表本次采购入库流程闭环，采购单同步更新为已入库(4)。
     *
     * @param purchaseId 采购单ID
     */
    private void updatePurchaseStatusAfterAcceptance(Long purchaseId) {
        Purchase updatePurchase = new Purchase();
        updatePurchase.setId(purchaseId);
        updatePurchase.setStatus(PurchaseStatusEnum.INBOUNDED.getCode());
        purchaseMapper.updateById(updatePurchase);
    }

    private void validatePurchaseStatusForInbound(Purchase purchase) {
        if (!PurchaseStatusEnum.APPROVED.getCode().equals(purchase.getStatus())
                && !PurchaseStatusEnum.PURCHASING.getCode().equals(purchase.getStatus())) {
            throw new BusinessException("只有已通过或采购中的采购单可以创建入库单");
        }
    }

    private void validateInboundType(Integer inboundType) {
        if (!InboundTypeEnum.NORMAL.getCode().equals(inboundType)
                && !InboundTypeEnum.DIRECT_FOOD.getCode().equals(inboundType)) {
            throw new BusinessException("入库类型不正确");
        }
    }

    private List<InboundItem> buildInboundItems(List<AddInboundItemDTO> items, Map<Long, PurchaseItem> purchaseItemMap) {
        return items.stream()
                .map(item -> {
                    PurchaseItem purchaseItem = purchaseItemMap.get(item.getProductId());
                    if (purchaseItem == null) {
                        throw new BusinessException("入库商品不属于该采购单");
                    }
                    int receivedQuantity = purchaseItem.getReceivedQuantity() == null ? 0 : purchaseItem.getReceivedQuantity();
                    int remainingQuantity = purchaseItem.getQuantity() - receivedQuantity;
                    if (item.getActualQuantity() > remainingQuantity) {
                        throw new BusinessException("商品【" + purchaseItem.getProductName() + "】入库数量超过剩余可入数量");
                    }
                    BigDecimal amount = purchaseItem.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getActualQuantity()))
                            .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                    InboundItem inboundItem = new InboundItem();
                    inboundItem.setProductId(item.getProductId());
                    inboundItem.setProductName(purchaseItem.getProductName());
                    inboundItem.setOrderQuantity(purchaseItem.getQuantity());
                    inboundItem.setActualQuantity(item.getActualQuantity());
                    inboundItem.setUnitPrice(purchaseItem.getUnitPrice());
                    inboundItem.setAmount(amount);
                    inboundItem.setBatchNo(item.getBatchNo());
                    inboundItem.setExpiryDate(item.getExpiryDate());
                    return inboundItem;
                })
                .toList();
    }

    private void handleNormalInbound(Inbound inbound, InboundItem inboundItem, User warehouseKeeper) {
        Inventory inventory = inventoryMapper.selectById(inboundItem.getProductId());
        int beforeQuantity = inventory == null ? 0 : inventory.getStockQuantity();
        BigDecimal beforeTotalCost = inventory == null ? BigDecimal.ZERO : inventory.getTotalCost();
        int afterQuantity = beforeQuantity + inboundItem.getActualQuantity();
        BigDecimal afterTotalCost = beforeTotalCost.add(inboundItem.getAmount()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal unitCost = afterTotalCost.divide(BigDecimal.valueOf(afterQuantity), MONEY_SCALE, RoundingMode.HALF_UP);
        Inventory updateInventory = new Inventory();
        updateInventory.setProductId(inboundItem.getProductId());
        updateInventory.setStockQuantity(afterQuantity);
        updateInventory.setUnitCost(unitCost);
        updateInventory.setTotalCost(afterTotalCost);
        updateInventory.setLastUpdateTime(LocalDateTime.now());
        if (inventory == null) {
            inventoryMapper.insert(updateInventory);
        } else {
            inventoryMapper.updateById(updateInventory);
        }
        insertInventoryRecord(inbound, inboundItem, warehouseKeeper,
                InventoryChangeTypeEnum.NORMAL_INBOUND, inboundItem.getActualQuantity(), beforeQuantity, afterQuantity);
    }

    private void handleDirectFoodInbound(Inbound inbound, InboundItem inboundItem, User warehouseKeeper) {
        Inventory inventory = inventoryMapper.selectById(inboundItem.getProductId());
        int currentQuantity = inventory == null ? 0 : inventory.getStockQuantity();
        insertInventoryRecord(inbound, inboundItem, warehouseKeeper,
                InventoryChangeTypeEnum.DIRECT_FOOD, -inboundItem.getActualQuantity(), currentQuantity, currentQuantity);
    }

    private void insertInventoryRecord(Inbound inbound,
                                       InboundItem inboundItem,
                                       User warehouseKeeper,
                                       InventoryChangeTypeEnum changeType,
                                       Integer changeQuantity,
                                       Integer beforeQuantity,
                                       Integer afterQuantity) {
        InventoryRecord record = new InventoryRecord();
        record.setProductId(inboundItem.getProductId());
        record.setProductName(inboundItem.getProductName());
        record.setChangeType(changeType.getCode().shortValue());
        record.setChangeQuantity(changeQuantity);
        record.setBeforeQuantity(beforeQuantity);
        record.setAfterQuantity(afterQuantity);
        record.setRelatedNo(inbound.getInboundNo());
        record.setOperatorId(warehouseKeeper.getId());
        record.setOperatorName(warehouseKeeper.getRealName());
        record.setRemark(inbound.getRemark());
        inventoryRecordMapper.insert(record);
    }

    private void updateReceivedQuantity(InboundItem inboundItem, Map<Long, PurchaseItem> purchaseItemMap) {
        PurchaseItem purchaseItem = purchaseItemMap.get(inboundItem.getProductId());
        int currentReceivedQuantity = purchaseItem.getReceivedQuantity() == null ? 0 : purchaseItem.getReceivedQuantity();
        int newReceivedQuantity = currentReceivedQuantity + inboundItem.getActualQuantity();
        purchaseItem.setReceivedQuantity(newReceivedQuantity);
        PurchaseItem updatePurchaseItem = new PurchaseItem();
        updatePurchaseItem.setId(purchaseItem.getId());
        updatePurchaseItem.setReceivedQuantity(newReceivedQuantity);
        purchaseItemMapper.updateById(updatePurchaseItem);
    }

    /**
     * 创建入库单后，若采购单仍为已通过(2)，则推进为采购中(3)。
     *
     * @param purchase 采购单
     */
    private void markPurchaseAsPurchasingIfApproved(Purchase purchase) {
        if (!PurchaseStatusEnum.APPROVED.getCode().equals(purchase.getStatus())) {
            return;
        }
        Purchase updatePurchase = new Purchase();
        updatePurchase.setId(purchase.getId());
        updatePurchase.setStatus(PurchaseStatusEnum.PURCHASING.getCode());
        purchaseMapper.updateById(updatePurchase);
    }

    private void validateDeptHead(Long deptHeadUserId, Purchase purchase) {
        User deptHead = assertUserExists(deptHeadUserId, "部门负责人不存在");
        if (RoleAuthSupport.isSuperManager(deptHead)) {
            return;
        }
        if (!RoleAuthSupport.hasRole(deptHead, RoleEnum.DEPT_HEAD)
                || !purchase.getDeptId().equals(deptHead.getDeptId())) {
            throw new BusinessException("验收人必须是申请部门负责人");
        }
    }

    private Purchase getExistingPurchase(Long purchaseId) {
        Purchase purchase = purchaseMapper.selectById(purchaseId);
        if (purchase == null) {
            throw new BusinessException("采购单不存在");
        }
        return purchase;
    }

    private Inbound getExistingInbound(Long inboundId) {
        Inbound inbound = inboundMapper.selectById(inboundId);
        if (inbound == null) {
            throw new BusinessException("入库单不存在");
        }
        return inbound;
    }

    private User assertUserExists(Long userId, String message) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(message);
        }
        return user;
    }

    private List<PurchaseItem> getPurchaseItems(Long purchaseId) {
        List<PurchaseItem> purchaseItems = purchaseItemMapper.selectList(
                new LambdaQueryWrapper<PurchaseItem>().eq(PurchaseItem::getPurchaseId, purchaseId)
        );
        if (purchaseItems.isEmpty()) {
            throw new BusinessException("采购单明细不存在");
        }
        return purchaseItems;
    }

    private Map<Long, PurchaseItem> buildPurchaseItemMap(List<PurchaseItem> purchaseItems) {
        Map<Long, PurchaseItem> purchaseItemMap = new HashMap<>();
        for (PurchaseItem purchaseItem : purchaseItems) {
            purchaseItemMap.put(purchaseItem.getProductId(), purchaseItem);
        }
        return purchaseItemMap;
    }

    private InboundListItemVO toListItemVO(Inbound inbound) {
        Purchase purchase = purchaseMapper.selectById(inbound.getPurchaseId());
        InboundListItemVO vo = new InboundListItemVO();
        vo.setInboundId(inbound.getId());
        vo.setInboundNo(inbound.getInboundNo());
        vo.setPurchaseId(inbound.getPurchaseId());
        vo.setPurchaseNo(purchase == null ? null : purchase.getPurchaseNo());
        vo.setSupplierName(getSupplierName(inbound.getSupplierId()));
        vo.setReceiverName(getUserRealName(inbound.getReceiverId()));
        vo.setInboundType(inbound.getInboundType().intValue());
        vo.setInboundTypeName(getInboundTypeName(inbound.getInboundType()));
        vo.setTotalAmount(inbound.getTotalAmount());
        vo.setStatus(inbound.getStatus().intValue());
        vo.setStatusName(getInboundStatusName(inbound.getStatus()));
        vo.setCreateTime(inbound.getCreateTime());
        return vo;
    }

    private InboundDetailVO toDetailVO(Inbound inbound) {
        Purchase purchase = purchaseMapper.selectById(inbound.getPurchaseId());
        InboundDetailVO vo = new InboundDetailVO();
        vo.setInboundId(inbound.getId());
        vo.setInboundNo(inbound.getInboundNo());
        vo.setPurchaseId(inbound.getPurchaseId());
        vo.setPurchaseNo(purchase == null ? null : purchase.getPurchaseNo());
        vo.setSupplierId(inbound.getSupplierId());
        vo.setSupplierName(getSupplierName(inbound.getSupplierId()));
        vo.setReceiverId(inbound.getReceiverId());
        vo.setReceiverName(getUserRealName(inbound.getReceiverId()));
        vo.setInboundType(inbound.getInboundType().intValue());
        vo.setInboundTypeName(getInboundTypeName(inbound.getInboundType()));
        vo.setTotalAmount(inbound.getTotalAmount());
        vo.setStatus(inbound.getStatus().intValue());
        vo.setStatusName(getInboundStatusName(inbound.getStatus()));
        vo.setDeptHeadId(inbound.getDeptHeadId());
        vo.setDeptHeadName(getUserRealName(inbound.getDeptHeadId()));
        vo.setAcceptTime(inbound.getAcceptTime());
        vo.setRemark(inbound.getRemark());
        vo.setCreateTime(inbound.getCreateTime());
        return vo;
    }

    private InboundItemVO toItemVO(InboundItem inboundItem) {
        InboundItemVO vo = new InboundItemVO();
        vo.setProductId(inboundItem.getProductId());
        vo.setProductName(inboundItem.getProductName());
        vo.setOrderQuantity(inboundItem.getOrderQuantity());
        vo.setActualQuantity(inboundItem.getActualQuantity());
        vo.setUnitPrice(inboundItem.getUnitPrice());
        vo.setAmount(inboundItem.getAmount());
        vo.setBatchNo(inboundItem.getBatchNo());
        vo.setExpiryDate(inboundItem.getExpiryDate());
        return vo;
    }

    private String getSupplierName(Long supplierId) {
        Supplier supplier = supplierId == null ? null : supplierMapper.selectById(supplierId);
        return supplier == null ? null : supplier.getSupplierName();
    }

    private String getUserRealName(Long userId) {
        User user = userId == null ? null : userMapper.selectById(userId);
        return user == null ? null : user.getRealName();
    }

    private String getInboundTypeName(Short inboundType) {
        if (toShort(InboundTypeEnum.NORMAL.getCode()).equals(inboundType)) {
            return InboundTypeEnum.NORMAL.getName();
        }
        if (toShort(InboundTypeEnum.DIRECT_FOOD.getCode()).equals(inboundType)) {
            return InboundTypeEnum.DIRECT_FOOD.getName();
        }
        return String.valueOf(inboundType);
    }

    private String getInboundStatusName(Short status) {
        if (toShort(InboundStatusEnum.PENDING_ACCEPTANCE.getCode()).equals(status)) {
            return InboundStatusEnum.PENDING_ACCEPTANCE.getName();
        }
        if (toShort(InboundStatusEnum.ACCEPTED.getCode()).equals(status)) {
            return InboundStatusEnum.ACCEPTED.getName();
        }
        return String.valueOf(status);
    }

    private Short toShort(Integer value) {
        return value == null ? null : value.shortValue();
    }
}
