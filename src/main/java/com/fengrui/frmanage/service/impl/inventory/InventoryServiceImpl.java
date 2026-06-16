package com.fengrui.frmanage.service.impl.inventory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import com.fengrui.frmanage.service.inventory.InventoryService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.inventory.InventoryRecordVO;
import com.fengrui.frmanage.vo.inventory.InventoryVO;
import com.fengrui.frmanage.vo.inventory.InventoryWarningVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 库存业务服务实现。
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private static final int MONEY_SCALE = 2;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InventoryMapper inventoryMapper;

    private final InventoryRecordMapper inventoryRecordMapper;

    private final ProductMapper productMapper;

    private final UserMapper userMapper;

    public InventoryServiceImpl(InventoryMapper inventoryMapper,
                                InventoryRecordMapper inventoryRecordMapper,
                                ProductMapper productMapper,
                                UserMapper userMapper) {
        this.inventoryMapper = inventoryMapper;
        this.inventoryRecordMapper = inventoryRecordMapper;
        this.productMapper = productMapper;
        this.userMapper = userMapper;
    }

    /**
     * 分页查询当前库存。
     *
     * @param queryDTO 查询参数
     * @return 当前库存分页数据
     */
    @Override
    public PageResultVO<InventoryVO> pageInventory(InventoryQueryDTO queryDTO) {
        IPage<InventoryVO> page = inventoryMapper.selectInventoryPage(
                Page.of(queryDTO.getPageNum(), queryDTO.getPageSize()),
                queryDTO.getProductName(),
                queryDTO.getCategory()
        );
        return new PageResultVO<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 分页查询库存预警。
     *
     * @param queryDTO 查询参数
     * @return 库存预警分页数据
     */
    @Override
    public PageResultVO<InventoryWarningVO> pageWarning(InventoryQueryDTO queryDTO) {
        IPage<InventoryWarningVO> page = inventoryMapper.selectWarningPage(
                Page.of(queryDTO.getPageNum(), queryDTO.getPageSize()),
                queryDTO.getProductName(),
                queryDTO.getCategory()
        );
        return new PageResultVO<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 分页查询库存流水。
     *
     * @param queryDTO 查询参数
     * @return 库存流水分页数据
     */
    @Override
    public PageResultVO<InventoryRecordVO> pageRecord(InventoryRecordQueryDTO queryDTO) {
        if (queryDTO.getChangeType() != null && InventoryChangeTypeEnum.getByCode(queryDTO.getChangeType()) == null) {
            throw new BusinessException("库存变动类型不正确");
        }
        LambdaQueryWrapper<InventoryRecord> queryWrapper = new LambdaQueryWrapper<InventoryRecord>()
                .eq(queryDTO.getProductId() != null, InventoryRecord::getProductId, queryDTO.getProductId())
                .eq(queryDTO.getChangeType() != null, InventoryRecord::getChangeType, toShort(queryDTO.getChangeType()))
                .ge(queryDTO.getStartTime() != null, InventoryRecord::getCreateTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, InventoryRecord::getCreateTime, queryDTO.getEndTime())
                .like(StringUtils.hasText(queryDTO.getRelatedNo()), InventoryRecord::getRelatedNo, queryDTO.getRelatedNo())
                .orderByDesc(InventoryRecord::getCreateTime)
                .orderByDesc(InventoryRecord::getId);
        IPage<InventoryRecord> page = inventoryRecordMapper.selectPage(
                Page.of(queryDTO.getPageNum(), queryDTO.getPageSize()),
                queryWrapper
        );
        List<InventoryRecordVO> records = page.getRecords().stream()
                .map(this::toRecordVO)
                .toList();
        return new PageResultVO<>(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    /**
     * 初始化库存。
     *
     * @param initInventoryDTO 初始化参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initInventory(InitInventoryDTO initInventoryDTO) {
        User operator = assertUserExists(initInventoryDTO.getOperatorUserId());
        for (InitInventoryItemDTO itemDTO : initInventoryDTO.getItems()) {
            Product product = getEnabledProduct(itemDTO.getProductId());
            assertNoBusinessInventoryRecord(product.getId(), product.getProductName());
            initSingleInventory(product, itemDTO, operator);
        }
    }

    private void initSingleInventory(Product product, InitInventoryItemDTO itemDTO, User operator) {
        Inventory currentInventory = inventoryMapper.selectById(product.getId());
        BigDecimal unitCost = normalizeMoney(itemDTO.getUnitCost());
        BigDecimal totalCost = calculateTotalCost(itemDTO.getInitialQuantity(), unitCost);
        if (currentInventory == null) {
            Inventory inventory = new Inventory();
            inventory.setProductId(product.getId());
            inventory.setStockQuantity(itemDTO.getInitialQuantity());
            inventory.setUnitCost(unitCost);
            inventory.setTotalCost(totalCost);
            inventory.setLastUpdateTime(java.time.LocalDateTime.now());
            inventoryMapper.insert(inventory);
            insertInitRecord(product, operator, itemDTO.getInitialQuantity(), 0, itemDTO.getInitialQuantity(), "期初库存初始化");
            return;
        }

        int beforeQuantity = currentInventory.getStockQuantity() == null ? 0 : currentInventory.getStockQuantity();
        BigDecimal beforeUnitCost = currentInventory.getUnitCost() == null ? BigDecimal.ZERO : normalizeMoney(currentInventory.getUnitCost());
        int delta = itemDTO.getInitialQuantity() - beforeQuantity;
        if (delta == 0 && beforeUnitCost.compareTo(unitCost) == 0) {
            return;
        }

        Inventory updateInventory = new Inventory();
        updateInventory.setProductId(product.getId());
        updateInventory.setStockQuantity(itemDTO.getInitialQuantity());
        updateInventory.setUnitCost(unitCost);
        updateInventory.setTotalCost(totalCost);
        updateInventory.setLastUpdateTime(java.time.LocalDateTime.now());
        inventoryMapper.updateById(updateInventory);
        insertInitRecord(product, operator, delta, beforeQuantity, itemDTO.getInitialQuantity(), "覆盖初始化");
    }

    private void insertInitRecord(Product product,
                                  User operator,
                                  Integer changeQuantity,
                                  Integer beforeQuantity,
                                  Integer afterQuantity,
                                  String remark) {
        InventoryRecord record = new InventoryRecord();
        record.setProductId(product.getId());
        record.setProductName(product.getProductName());
        record.setChangeType(InventoryChangeTypeEnum.INITIAL_STOCK.getCode().shortValue());
        record.setChangeQuantity(changeQuantity);
        record.setBeforeQuantity(beforeQuantity);
        record.setAfterQuantity(afterQuantity);
        record.setRelatedNo("INIT-" + LocalDate.now().format(DATE_FORMATTER));
        record.setOperatorId(operator.getId());
        record.setOperatorName(operator.getRealName());
        record.setRemark(remark);
        inventoryRecordMapper.insert(record);
    }

    private void assertNoBusinessInventoryRecord(Long productId, String productName) {
        Long businessRecordCount = inventoryRecordMapper.selectCount(
                new LambdaQueryWrapper<InventoryRecord>()
                        .eq(InventoryRecord::getProductId, productId)
                        .ne(InventoryRecord::getChangeType, InventoryChangeTypeEnum.INITIAL_STOCK.getCode().shortValue())
        );
        if (businessRecordCount != null && businessRecordCount > 0) {
            throw new BusinessException("商品【" + productName + "】已存在业务库存流水，不允许初始化覆盖");
        }
    }

    private Product getEnabledProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !toShort(ProductStatusEnum.ENABLED.getCode()).equals(product.getStatus())) {
            throw new BusinessException("商品不存在或未启用");
        }
        return product;
    }

    private User assertUserExists(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("操作人不存在");
        }
        return user;
    }

    private InventoryRecordVO toRecordVO(InventoryRecord record) {
        InventoryRecordVO vo = new InventoryRecordVO();
        vo.setRecordId(record.getId());
        vo.setProductId(record.getProductId());
        vo.setProductName(record.getProductName());
        vo.setChangeType(record.getChangeType().intValue());
        vo.setChangeTypeName(InventoryChangeTypeEnum.getDescriptionByCode(record.getChangeType().intValue()));
        vo.setChangeQuantity(record.getChangeQuantity());
        vo.setBeforeQuantity(record.getBeforeQuantity());
        vo.setAfterQuantity(record.getAfterQuantity());
        vo.setRelatedNo(record.getRelatedNo());
        vo.setOperatorId(record.getOperatorId());
        vo.setOperatorName(record.getOperatorName());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    private BigDecimal calculateTotalCost(Integer quantity, BigDecimal unitCost) {
        return unitCost.multiply(BigDecimal.valueOf(quantity)).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private Short toShort(Integer value) {
        return value == null ? null : value.shortValue();
    }
}
