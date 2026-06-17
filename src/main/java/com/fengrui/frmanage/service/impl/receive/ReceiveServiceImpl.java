package com.fengrui.frmanage.service.impl.receive;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.frmanage.common.enums.BizErrorCode;
import com.fengrui.frmanage.common.enums.InventoryChangeTypeEnum;
import com.fengrui.frmanage.common.enums.ProductStatusEnum;
import com.fengrui.frmanage.common.enums.ReceiveStatusEnum;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.dto.receive.AddReceiveDTO;
import com.fengrui.frmanage.dto.receive.AddReceiveItemDTO;
import com.fengrui.frmanage.dto.receive.ReceiveApproveDTO;
import com.fengrui.frmanage.dto.receive.ReceiveCancelDTO;
import com.fengrui.frmanage.dto.receive.ReceiveConfirmDTO;
import com.fengrui.frmanage.dto.receive.ReceiveListQueryDTO;
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
import com.fengrui.frmanage.service.receive.ReceiveNoService;
import com.fengrui.frmanage.service.receive.ReceiveService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.receive.ReceiveDetailVO;
import com.fengrui.frmanage.vo.receive.ReceiveItemVO;
import com.fengrui.frmanage.vo.receive.ReceiveListItemVO;
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
 * 领用业务服务实现。
 */
@Service
public class ReceiveServiceImpl implements ReceiveService {

    private static final int MONEY_SCALE = 2;

    private final ReceiveMapper receiveMapper;

    private final ReceiveItemMapper receiveItemMapper;

    private final DepartmentMapper departmentMapper;

    private final UserMapper userMapper;

    private final ProductMapper productMapper;

    private final InventoryMapper inventoryMapper;

    private final InventoryRecordMapper inventoryRecordMapper;

    private final ReceiveNoService receiveNoService;

    public ReceiveServiceImpl(ReceiveMapper receiveMapper,
                              ReceiveItemMapper receiveItemMapper,
                              DepartmentMapper departmentMapper,
                              UserMapper userMapper,
                              ProductMapper productMapper,
                              InventoryMapper inventoryMapper,
                              InventoryRecordMapper inventoryRecordMapper,
                              ReceiveNoService receiveNoService) {
        this.receiveMapper = receiveMapper;
        this.receiveItemMapper = receiveItemMapper;
        this.departmentMapper = departmentMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.inventoryMapper = inventoryMapper;
        this.inventoryRecordMapper = inventoryRecordMapper;
        this.receiveNoService = receiveNoService;
    }

    /**
     * 新增领用单。
     *
     * @param addReceiveDTO 新增领用单参数
     * @return 领用单ID和单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addReceive(AddReceiveDTO addReceiveDTO) {
        validateDepartment(addReceiveDTO.getDeptId());
        User applyUser = assertUserExists(addReceiveDTO.getApplyUserId(), "申请人不存在");
        if (!addReceiveDTO.getDeptId().equals(applyUser.getDeptId())) {
            throw new BusinessException("申请人所属部门必须与领用部门一致");
        }

        List<ReceiveItem> receiveItems = buildReceiveItems(addReceiveDTO.getItems());
        BigDecimal totalAmount = receiveItems.stream()
                .map(ReceiveItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        String receiveNo = receiveNoService.generateReceiveNo();
        Receive receive = new Receive();
        receive.setReceiveNo(receiveNo);
        receive.setDeptId(addReceiveDTO.getDeptId());
        receive.setApplyUserId(addReceiveDTO.getApplyUserId());
        receive.setApplyTime(LocalDateTime.now());
        receive.setPurpose(addReceiveDTO.getPurpose());
        receive.setTotalAmount(totalAmount);
        receive.setStatus(toShort(ReceiveStatusEnum.PENDING_APPROVAL.getCode()));
        receive.setRemark(addReceiveDTO.getRemark());
        receiveMapper.insert(receive);

        for (ReceiveItem receiveItem : receiveItems) {
            receiveItem.setReceiveId(receive.getId());
            receiveItemMapper.insert(receiveItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("receiveId", receive.getId());
        result.put("receiveNo", receiveNo);
        return result;
    }

    /**
     * 分页查询领用单。
     *
     * @param queryDTO 查询参数
     * @return 领用单分页列表
     */
    @Override
    public PageResultVO<ReceiveListItemVO> pageList(ReceiveListQueryDTO queryDTO) {
        LambdaQueryWrapper<Receive> queryWrapper = new LambdaQueryWrapper<Receive>()
                .eq(queryDTO.getStatus() != null, Receive::getStatus, toShort(queryDTO.getStatus()))
                .eq(queryDTO.getDeptId() != null, Receive::getDeptId, queryDTO.getDeptId())
                .eq(queryDTO.getApplyUserId() != null, Receive::getApplyUserId, queryDTO.getApplyUserId())
                .ge(queryDTO.getStartTime() != null, Receive::getApplyTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, Receive::getApplyTime, queryDTO.getEndTime())
                .orderByDesc(Receive::getId);
        IPage<Receive> receivePage = receiveMapper.selectPage(
                Page.of(queryDTO.getPageNum(), queryDTO.getPageSize()),
                queryWrapper
        );
        List<ReceiveListItemVO> records = receivePage.getRecords()
                .stream()
                .map(this::toListItemVO)
                .toList();
        return new PageResultVO<>(receivePage.getTotal(), receivePage.getCurrent(), receivePage.getSize(), records);
    }

    /**
     * 查询领用单详情。
     *
     * @param receiveId 领用单ID
     * @return 领用单详情
     */
    @Override
    public ReceiveDetailVO detail(Long receiveId) {
        Receive receive = getExistingReceive(receiveId);
        List<ReceiveItem> items = receiveItemMapper.selectList(
                new LambdaQueryWrapper<ReceiveItem>().eq(ReceiveItem::getReceiveId, receiveId)
        );
        ReceiveDetailVO detailVO = toDetailVO(receive);
        detailVO.setItems(items.stream().map(this::toItemVO).toList());
        return detailVO;
    }

    /**
     * 审批领用单。
     *
     * @param approveDTO 审批参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(ReceiveApproveDTO approveDTO) {
        Receive receive = getExistingReceive(approveDTO.getReceiveId());
        assertStatus(receive, ReceiveStatusEnum.PENDING_APPROVAL, "只有待审批领用单可以审批");
        validateDeptHead(approveDTO.getApproverUserId(), receive.getDeptId());
        if (Boolean.FALSE.equals(approveDTO.getApproved()) && !StringUtils.hasText(approveDTO.getRemark())) {
            throw new BusinessException("驳回时必须填写驳回理由");
        }

        Receive updateReceive = new Receive();
        updateReceive.setId(receive.getId());
        updateReceive.setApproverId(approveDTO.getApproverUserId());
        updateReceive.setApproveTime(LocalDateTime.now());
        updateReceive.setRemark(approveDTO.getRemark());
        updateReceive.setStatus(Boolean.TRUE.equals(approveDTO.getApproved())
                ? toShort(ReceiveStatusEnum.PENDING_OUTBOUND.getCode())
                : toShort(ReceiveStatusEnum.REJECTED.getCode()));
        receiveMapper.updateById(updateReceive);
    }

    /**
     * 确认出库。
     *
     * @param confirmDTO 出库确认参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(ReceiveConfirmDTO confirmDTO) {
        Receive receive = getExistingReceive(confirmDTO.getReceiveId());
        assertStatus(receive, ReceiveStatusEnum.PENDING_OUTBOUND, "只有待出库领用单可以出库");
        User deliverer = assertUserExists(confirmDTO.getDelivererUserId(), "出库发货人不存在");
        assertAnyRole(deliverer, RoleEnum.WAREHOUSE_KEEPER, RoleEnum.ADMIN);
        List<ReceiveItem> items = receiveItemMapper.selectList(
                new LambdaQueryWrapper<ReceiveItem>().eq(ReceiveItem::getReceiveId, receive.getId())
        );

        // TODO 后续库存并发扣减建议使用 SELECT ... FOR UPDATE 或 inventory.version 乐观锁。
        for (ReceiveItem item : items) {
            Inventory inventory = inventoryMapper.selectById(item.getProductId());
            if (inventory == null || inventory.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException("商品【" + item.getProductName() + "】库存不足");
            }
        }
        for (ReceiveItem item : items) {
            deductInventory(receive, item, deliverer);
        }

        Receive updateReceive = new Receive();
        updateReceive.setId(receive.getId());
        updateReceive.setStatus(toShort(ReceiveStatusEnum.OUTBOUNDED.getCode()));
        updateReceive.setDelivererId(confirmDTO.getDelivererUserId());
        updateReceive.setDeliverTime(LocalDateTime.now());
        updateReceive.setRemark(StringUtils.hasText(confirmDTO.getRemark()) ? confirmDTO.getRemark() : receive.getRemark());
        receiveMapper.updateById(updateReceive);
        // TODO 后续扩展部门成本记录。
    }

    /**
     * 取消领用单。
     *
     * @param cancelDTO 取消参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(ReceiveCancelDTO cancelDTO) {
        Receive receive = getExistingReceive(cancelDTO.getReceiveId());
        User operator = assertUserExists(cancelDTO.getOperatorUserId(), "操作人不存在");
        assertApplicantOrAdmin(operator, receive);
        if (!isCancelableStatus(receive.getStatus())) {
            throw new BusinessException("当前领用单状态不可取消");
        }
        Receive updateReceive = new Receive();
        updateReceive.setId(receive.getId());
        updateReceive.setStatus(toShort(ReceiveStatusEnum.CANCELLED.getCode()));
        receiveMapper.updateById(updateReceive);
    }

    private List<ReceiveItem> buildReceiveItems(List<AddReceiveItemDTO> itemDTOList) {
        return itemDTOList.stream()
                .map(itemDTO -> {
                    Product product = productMapper.selectById(itemDTO.getProductId());
                    Short enabledStatus = ProductStatusEnum.ENABLED.getCode().shortValue();
                    if (product == null || !enabledStatus.equals(product.getStatus())) {
                        throw new BusinessException("商品不存在");
                    }
                    Inventory inventory = inventoryMapper.selectById(itemDTO.getProductId());
                    if (inventory == null) {
                        throw new BusinessException("商品【" + product.getProductName() + "】库存不存在");
                    }
                    BigDecimal unitPrice = inventory.getUnitCost();
                    BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()))
                            .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                    ReceiveItem receiveItem = new ReceiveItem();
                    receiveItem.setProductId(product.getId());
                    receiveItem.setProductName(product.getProductName());
                    receiveItem.setQuantity(itemDTO.getQuantity());
                    receiveItem.setUnitPrice(unitPrice);
                    receiveItem.setAmount(amount);
                    return receiveItem;
                })
                .toList();
    }

    private void deductInventory(Receive receive, ReceiveItem item, User deliverer) {
        Inventory inventory = inventoryMapper.selectById(item.getProductId());
        int beforeQuantity = inventory.getStockQuantity();
        int afterQuantity = beforeQuantity - item.getQuantity();
        BigDecimal deductAmount = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal afterTotalCost = inventory.getTotalCost().subtract(deductAmount).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (afterQuantity == 0) {
            afterTotalCost = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }

        Inventory updateInventory = new Inventory();
        updateInventory.setProductId(item.getProductId());
        updateInventory.setStockQuantity(afterQuantity);
        updateInventory.setUnitCost(inventory.getUnitCost());
        updateInventory.setTotalCost(afterTotalCost);
        updateInventory.setLastUpdateTime(LocalDateTime.now());
        inventoryMapper.updateById(updateInventory);

        InventoryRecord record = new InventoryRecord();
        record.setProductId(item.getProductId());
        record.setProductName(item.getProductName());
        record.setChangeType(InventoryChangeTypeEnum.NORMAL_OUTBOUND.getCode().shortValue());
        record.setChangeQuantity(-item.getQuantity());
        record.setBeforeQuantity(beforeQuantity);
        record.setAfterQuantity(afterQuantity);
        record.setRelatedNo(receive.getReceiveNo());
        record.setOperatorId(deliverer.getId());
        record.setOperatorName(deliverer.getRealName());
        record.setRemark(receive.getRemark());
        inventoryRecordMapper.insert(record);
    }

    private void validateDepartment(Long deptId) {
        if (departmentMapper.selectById(deptId) == null) {
            throw new BusinessException("领用部门不存在");
        }
    }

    private void validateDeptHead(Long userId, Long deptId) {
        User deptHead = assertUserExists(userId, "审批人不存在");
        if (!RoleEnum.DEPT_HEAD.getCode().equals(deptHead.getRole()) || !deptId.equals(deptHead.getDeptId())) {
            throw new BusinessException("审批人必须是领用部门负责人");
        }
    }

    private Receive getExistingReceive(Long receiveId) {
        Receive receive = receiveMapper.selectById(receiveId);
        if (receive == null) {
            throw new BusinessException("领用单不存在");
        }
        return receive;
    }

    private User assertUserExists(Long userId, String message) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(message);
        }
        return user;
    }

    /**
     * 校验操作人是领用申请人本人或管理员。
     *
     * @param operator 操作人
     * @param receive 领用单
     */
    private void assertApplicantOrAdmin(User operator, Receive receive) {
        if (hasRole(operator, RoleEnum.ADMIN) || receive.getApplyUserId().equals(operator.getId())) {
            return;
        }
        throw new BusinessException(BizErrorCode.AUTH_FORBIDDEN);
    }

    /**
     * 校验用户具备任一角色。
     *
     * @param user 用户
     * @param roles 允许角色
     */
    private void assertAnyRole(User user, RoleEnum... roles) {
        for (RoleEnum role : roles) {
            if (hasRole(user, role)) {
                return;
            }
        }
        throw new BusinessException(BizErrorCode.AUTH_FORBIDDEN);
    }

    /**
     * 判断用户是否具备指定角色。
     *
     * @param user 用户
     * @param role 角色
     * @return 是否具备指定角色
     */
    private boolean hasRole(User user, RoleEnum role) {
        return role.getCode().equals(user.getRole());
    }

    private void assertStatus(Receive receive, ReceiveStatusEnum expectedStatus, String message) {
        if (!toShort(expectedStatus.getCode()).equals(receive.getStatus())) {
            throw new BusinessException(message);
        }
    }

    private boolean isCancelableStatus(Short status) {
        return toShort(ReceiveStatusEnum.PENDING_APPROVAL.getCode()).equals(status)
                || toShort(ReceiveStatusEnum.PENDING_OUTBOUND.getCode()).equals(status);
    }

    private ReceiveListItemVO toListItemVO(Receive receive) {
        ReceiveListItemVO vo = new ReceiveListItemVO();
        vo.setReceiveId(receive.getId());
        vo.setReceiveNo(receive.getReceiveNo());
        vo.setDeptId(receive.getDeptId());
        vo.setDeptName(getDepartmentName(receive.getDeptId()));
        vo.setApplyUserId(receive.getApplyUserId());
        vo.setApplyUserRealName(getUserRealName(receive.getApplyUserId()));
        vo.setApplyTime(receive.getApplyTime());
        vo.setPurpose(receive.getPurpose());
        vo.setTotalAmount(receive.getTotalAmount());
        vo.setStatus(receive.getStatus().intValue());
        vo.setStatusName(ReceiveStatusEnum.getNameByCode(receive.getStatus().intValue()));
        return vo;
    }

    private ReceiveDetailVO toDetailVO(Receive receive) {
        ReceiveDetailVO vo = new ReceiveDetailVO();
        vo.setReceiveId(receive.getId());
        vo.setReceiveNo(receive.getReceiveNo());
        vo.setDeptId(receive.getDeptId());
        vo.setDeptName(getDepartmentName(receive.getDeptId()));
        vo.setApplyUserId(receive.getApplyUserId());
        vo.setApplyUserRealName(getUserRealName(receive.getApplyUserId()));
        vo.setApplyTime(receive.getApplyTime());
        vo.setPurpose(receive.getPurpose());
        vo.setTotalAmount(receive.getTotalAmount());
        vo.setStatus(receive.getStatus().intValue());
        vo.setStatusName(ReceiveStatusEnum.getNameByCode(receive.getStatus().intValue()));
        vo.setApproverId(receive.getApproverId());
        vo.setApproverRealName(getUserRealName(receive.getApproverId()));
        vo.setApproveTime(receive.getApproveTime());
        vo.setDelivererId(receive.getDelivererId());
        vo.setDelivererName(getUserRealName(receive.getDelivererId()));
        vo.setDeliverTime(receive.getDeliverTime());
        vo.setRemark(receive.getRemark());
        return vo;
    }

    private ReceiveItemVO toItemVO(ReceiveItem item) {
        ReceiveItemVO vo = new ReceiveItemVO();
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setQuantity(item.getQuantity());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setAmount(item.getAmount());
        return vo;
    }

    private String getDepartmentName(Long deptId) {
        Department department = deptId == null ? null : departmentMapper.selectById(deptId);
        return department == null ? null : department.getDeptName();
    }

    private String getUserRealName(Long userId) {
        User user = userId == null ? null : userMapper.selectById(userId);
        return user == null ? null : user.getRealName();
    }

    private Short toShort(Integer value) {
        return value == null ? null : value.shortValue();
    }
}
