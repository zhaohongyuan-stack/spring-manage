package com.fengrui.frmanage.service.impl.purchase;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.frmanage.common.enums.BizErrorCode;
import com.fengrui.frmanage.common.enums.ProductStatusEnum;
import com.fengrui.frmanage.common.enums.PurchaseStatusEnum;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.dto.purchase.AddPurchaseDTO;
import com.fengrui.frmanage.dto.purchase.AddPurchaseItemDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseApproveDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseCancelDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseConfirmDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseListQueryDTO;
import com.fengrui.frmanage.entity.Department;
import com.fengrui.frmanage.entity.Inbound;
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
import com.fengrui.frmanage.service.purchase.PurchaseNoService;
import com.fengrui.frmanage.service.purchase.PurchaseService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.purchase.PurchaseDetailVO;
import com.fengrui.frmanage.vo.purchase.PurchaseItemVO;
import com.fengrui.frmanage.vo.purchase.PurchaseListItemVO;
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
 * 采购业务服务实现。
 */
@Service
public class PurchaseServiceImpl implements PurchaseService {

    private static final int MONEY_SCALE = 2;

    private final PurchaseMapper purchaseMapper;

    private final PurchaseItemMapper purchaseItemMapper;

    private final InboundMapper inboundMapper;

    private final DepartmentMapper departmentMapper;

    private final UserMapper userMapper;

    private final SupplierMapper supplierMapper;

    private final ProductMapper productMapper;

    private final PurchaseNoService purchaseNoService;

    public PurchaseServiceImpl(PurchaseMapper purchaseMapper,
                               PurchaseItemMapper purchaseItemMapper,
                               InboundMapper inboundMapper,
                               DepartmentMapper departmentMapper,
                               UserMapper userMapper,
                               SupplierMapper supplierMapper,
                               ProductMapper productMapper,
                               PurchaseNoService purchaseNoService) {
        this.purchaseMapper = purchaseMapper;
        this.purchaseItemMapper = purchaseItemMapper;
        this.inboundMapper = inboundMapper;
        this.departmentMapper = departmentMapper;
        this.userMapper = userMapper;
        this.supplierMapper = supplierMapper;
        this.productMapper = productMapper;
        this.purchaseNoService = purchaseNoService;
    }

    /**
     * 新增采购单。
     *
     * @param addPurchaseDTO 新增采购单参数
     * @return 采购单ID和单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addPurchase(AddPurchaseDTO addPurchaseDTO) {
        validatePurchaseHeader(addPurchaseDTO);
        String purchaseNo = purchaseNoService.generatePurchaseNo();
        Map<Long, Product> productMap = loadProductMap(addPurchaseDTO.getItems());
        List<PurchaseItem> purchaseItems = buildPurchaseItems(addPurchaseDTO.getItems(), productMap);
        BigDecimal totalAmount = purchaseItems.stream()
                .map(PurchaseItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        Purchase purchase = new Purchase();
        purchase.setPurchaseNo(purchaseNo);
        purchase.setDeptId(addPurchaseDTO.getDeptId());
        purchase.setApplyUserId(addPurchaseDTO.getApplyUserId());
        purchase.setApplyTime(LocalDateTime.now());
        purchase.setRequireDate(addPurchaseDTO.getRequireDate());
        purchase.setSupplierId(addPurchaseDTO.getSupplierId());
        purchase.setTotalAmount(totalAmount);
        purchase.setStatus(PurchaseStatusEnum.PENDING_APPROVAL.getCode());
        purchase.setRemark(addPurchaseDTO.getRemark());
        purchaseMapper.insert(purchase);
        Long purchaseId = resolvePurchaseIdAfterInsert(purchase);

        for (PurchaseItem purchaseItem : purchaseItems) {
            purchaseItem.setPurchaseId(purchaseId);
            purchaseItemMapper.insert(purchaseItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("purchaseId", purchaseId);
        result.put("purchaseNo", purchaseNo);
        return result;
    }

    /**
     * 分页查询采购单。
     *
     * @param queryDTO 查询参数
     * @return 采购单分页列表
     */
    @Override
    public PageResultVO<PurchaseListItemVO> pageList(PurchaseListQueryDTO queryDTO) {
        LambdaQueryWrapper<Purchase> queryWrapper = new LambdaQueryWrapper<Purchase>()
                .eq(queryDTO.getStatus() != null, Purchase::getStatus, queryDTO.getStatus())
                .like(StringUtils.hasText(queryDTO.getPurchaseNo()), Purchase::getPurchaseNo, queryDTO.getPurchaseNo())
                .orderByDesc(Purchase::getId);
        IPage<Purchase> purchasePage = purchaseMapper.selectPage(
                Page.of(queryDTO.getPageNum(), queryDTO.getPageSize()),
                queryWrapper
        );
        List<PurchaseListItemVO> records = purchasePage.getRecords()
                .stream()
                .map(this::toListItemVO)
                .toList();
        return new PageResultVO<>(purchasePage.getTotal(), purchasePage.getCurrent(), purchasePage.getSize(), records);
    }

    /**
     * 查询采购单详情。
     *
     * @param purchaseId 采购单ID
     * @return 采购单详情
     */
    @Override
    public PurchaseDetailVO detail(Long purchaseId) {
        Purchase purchase = getExistingPurchase(purchaseId);
        List<PurchaseItem> items = purchaseItemMapper.selectList(
                new LambdaQueryWrapper<PurchaseItem>().eq(PurchaseItem::getPurchaseId, purchaseId)
        );
        PurchaseDetailVO detailVO = toDetailVO(purchase);
        detailVO.setItems(items.stream().map(this::toItemVO).toList());
        return detailVO;
    }

    /**
     * 审批采购单。
     *
     * @param approveDTO 审批参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(PurchaseApproveDTO approveDTO) {
        Purchase purchase = getExistingPurchase(approveDTO.getPurchaseId());
        assertStatus(purchase, PurchaseStatusEnum.PENDING_APPROVAL, "只有待审批采购单可以审批");
        User approver = assertUserExists(approveDTO.getApproverUserId(), "审批人不存在");
        assertAdminOrDeptHeadOfPurchase(approver, purchase);
        if (Boolean.FALSE.equals(approveDTO.getApproved()) && !StringUtils.hasText(approveDTO.getRemark())) {
            throw new BusinessException("驳回时必须填写驳回理由");
        }

        Purchase updatePurchase = new Purchase();
        updatePurchase.setId(purchase.getId());
        updatePurchase.setApproverId(approveDTO.getApproverUserId());
        updatePurchase.setApproveTime(LocalDateTime.now());
        updatePurchase.setRemark(approveDTO.getRemark());
        updatePurchase.setStatus(Boolean.TRUE.equals(approveDTO.getApproved())
                ? PurchaseStatusEnum.APPROVED.getCode()
                : PurchaseStatusEnum.REJECTED.getCode());
        purchaseMapper.updateById(updatePurchase);
    }

    /**
     * 确认采购。
     *
     * @param confirmDTO 采购确认参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(PurchaseConfirmDTO confirmDTO) {
        Purchase purchase = getExistingPurchase(confirmDTO.getPurchaseId());
        assertStatus(purchase, PurchaseStatusEnum.APPROVED, "只有已通过采购单可以确认采购");
        User purchaser = assertUserExists(confirmDTO.getPurchaserUserId(), "采购员不存在");
        assertAnyRole(purchaser, RoleEnum.PURCHASER, RoleEnum.ADMIN);

        Purchase updatePurchase = new Purchase();
        updatePurchase.setId(purchase.getId());
        updatePurchase.setPurchaserId(confirmDTO.getPurchaserUserId());
        updatePurchase.setPurchaseConfirmTime(LocalDateTime.now());
        updatePurchase.setStatus(PurchaseStatusEnum.PURCHASING.getCode());
        purchaseMapper.updateById(updatePurchase);
    }

    /**
     * 取消采购单。
     *
     * @param cancelDTO 取消参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(PurchaseCancelDTO cancelDTO) {
        Purchase purchase = getExistingPurchase(cancelDTO.getPurchaseId());
        User operator = assertUserExists(cancelDTO.getOperatorUserId(), "操作人不存在");
        assertCanCancelPurchase(operator, purchase);
        if (!isCancelableStatus(purchase.getStatus())) {
            throw new BusinessException("当前采购单状态不可取消");
        }
        Long inboundCount = inboundMapper.selectCount(
                new LambdaQueryWrapper<Inbound>().eq(Inbound::getPurchaseId, purchase.getId())
        );
        if (inboundCount > 0) {
            throw new BusinessException("采购单已存在关联入库单，不允许取消");
        }

        Purchase updatePurchase = new Purchase();
        updatePurchase.setId(purchase.getId());
        updatePurchase.setStatus(PurchaseStatusEnum.CANCELLED.getCode());
        purchaseMapper.updateById(updatePurchase);
    }

    private void validatePurchaseHeader(AddPurchaseDTO addPurchaseDTO) {
        if (departmentMapper.selectById(addPurchaseDTO.getDeptId()) == null) {
            throw new BusinessException(BizErrorCode.PURCHASE_DEPT_NOT_FOUND);
        }
        assertActiveUserExists(addPurchaseDTO.getApplyUserId());
        if (supplierMapper.selectById(addPurchaseDTO.getSupplierId()) == null) {
            throw new BusinessException(BizErrorCode.PURCHASE_SUPPLIER_NOT_FOUND);
        }
    }

    private Map<Long, Product> loadProductMap(List<AddPurchaseItemDTO> items) {
        Map<Long, Product> productMap = new HashMap<>();
        for (AddPurchaseItemDTO item : items) {
            Product product = productMapper.selectById(item.getProductId());
            Short disabledStatus = ProductStatusEnum.DISABLED.getCode().shortValue();
            if (product == null || disabledStatus.equals(product.getStatus())) {
                throw new BusinessException(BizErrorCode.PURCHASE_PRODUCT_NOT_FOUND);
            }
            productMap.put(product.getId(), product);
        }
        return productMap;
    }

    private List<PurchaseItem> buildPurchaseItems(List<AddPurchaseItemDTO> items, Map<Long, Product> productMap) {
        return items.stream()
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    BigDecimal amount = item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
                            .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                    PurchaseItem purchaseItem = new PurchaseItem();
                    purchaseItem.setProductId(item.getProductId());
                    purchaseItem.setProductName(product.getProductName());
                    purchaseItem.setQuantity(item.getQuantity());
                    purchaseItem.setUnitPrice(item.getUnitPrice());
                    purchaseItem.setAmount(amount);
                    purchaseItem.setReceivedQuantity(0);
                    return purchaseItem;
                })
                .toList();
    }

    private Purchase getExistingPurchase(Long purchaseId) {
        Purchase purchase = purchaseMapper.selectById(purchaseId);
        if (purchase == null) {
            throw new BusinessException("采购单不存在");
        }
        return purchase;
    }

    private User assertUserExists(Long userId, String message) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(400, message);
        }
        return user;
    }

    /**
     * 校验申请人存在且未禁用。
     *
     * @param userId 用户ID
     */
    private void assertActiveUserExists(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(BizErrorCode.PURCHASE_USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(BizErrorCode.PURCHASE_USER_DISABLED);
        }
    }

    /**
     * 插入采购单后解析主键ID。
     *
     * @param purchase 已插入的采购单
     * @return 采购单主键ID
     */
    private Long resolvePurchaseIdAfterInsert(Purchase purchase) {
        if (purchase.getId() != null) {
            return purchase.getId();
        }
        Purchase savedPurchase = purchaseMapper.selectOne(
                new LambdaQueryWrapper<Purchase>()
                        .eq(Purchase::getPurchaseNo, purchase.getPurchaseNo())
                        .orderByDesc(Purchase::getId)
                        .last("LIMIT 1")
        );
        if (savedPurchase == null || savedPurchase.getId() == null) {
            throw new BusinessException(BizErrorCode.PURCHASE_SAVE_FAILED);
        }
        return savedPurchase.getId();
    }

    /**
     * 校验审批人是管理员或申请部门负责人。
     *
     * @param approver 审批人
     * @param purchase 采购单
     */
    private void assertAdminOrDeptHeadOfPurchase(User approver, Purchase purchase) {
        if (hasRole(approver, RoleEnum.ADMIN)) {
            return;
        }
        if (hasRole(approver, RoleEnum.DEPT_HEAD) && purchase.getDeptId().equals(approver.getDeptId())) {
            return;
        }
        throw new BusinessException(BizErrorCode.AUTH_FORBIDDEN);
    }

    /**
     * 校验取消采购单权限。
     *
     * @param operator 操作人
     * @param purchase 采购单
     */
    private void assertCanCancelPurchase(User operator, Purchase purchase) {
        if (hasRole(operator, RoleEnum.ADMIN)
                || hasRole(operator, RoleEnum.PURCHASER)
                || purchase.getApplyUserId().equals(operator.getId())) {
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

    private void assertStatus(Purchase purchase, PurchaseStatusEnum expectedStatus, String message) {
        if (!expectedStatus.getCode().equals(purchase.getStatus())) {
            throw new BusinessException(message);
        }
    }

    private boolean isCancelableStatus(Integer status) {
        return PurchaseStatusEnum.PENDING_APPROVAL.getCode().equals(status)
                || PurchaseStatusEnum.APPROVED.getCode().equals(status)
                || PurchaseStatusEnum.PURCHASING.getCode().equals(status);
    }

    private PurchaseListItemVO toListItemVO(Purchase purchase) {
        PurchaseListItemVO listItemVO = new PurchaseListItemVO();
        listItemVO.setPurchaseId(purchase.getId());
        listItemVO.setPurchaseNo(purchase.getPurchaseNo());
        listItemVO.setDeptId(purchase.getDeptId());
        listItemVO.setDeptName(getDepartmentName(purchase.getDeptId()));
        listItemVO.setApplyUserId(purchase.getApplyUserId());
        listItemVO.setApplyUserRealName(getUserRealName(purchase.getApplyUserId()));
        listItemVO.setApplyTime(purchase.getApplyTime());
        listItemVO.setRequireDate(purchase.getRequireDate());
        listItemVO.setSupplierId(purchase.getSupplierId());
        listItemVO.setSupplierName(getSupplierName(purchase.getSupplierId()));
        listItemVO.setTotalAmount(purchase.getTotalAmount());
        listItemVO.setStatus(purchase.getStatus());
        listItemVO.setStatusName(PurchaseStatusEnum.getNameByCode(purchase.getStatus()));
        return listItemVO;
    }

    private PurchaseDetailVO toDetailVO(Purchase purchase) {
        PurchaseDetailVO detailVO = new PurchaseDetailVO();
        detailVO.setPurchaseId(purchase.getId());
        detailVO.setPurchaseNo(purchase.getPurchaseNo());
        detailVO.setDeptId(purchase.getDeptId());
        detailVO.setDeptName(getDepartmentName(purchase.getDeptId()));
        detailVO.setApplyUserId(purchase.getApplyUserId());
        detailVO.setApplyUserRealName(getUserRealName(purchase.getApplyUserId()));
        detailVO.setApplyTime(purchase.getApplyTime());
        detailVO.setRequireDate(purchase.getRequireDate());
        detailVO.setSupplierId(purchase.getSupplierId());
        detailVO.setSupplierName(getSupplierName(purchase.getSupplierId()));
        detailVO.setTotalAmount(purchase.getTotalAmount());
        detailVO.setStatus(purchase.getStatus());
        detailVO.setStatusName(PurchaseStatusEnum.getNameByCode(purchase.getStatus()));
        detailVO.setApproverId(purchase.getApproverId());
        detailVO.setApproverRealName(getUserRealName(purchase.getApproverId()));
        detailVO.setApproveTime(purchase.getApproveTime());
        detailVO.setPurchaserId(purchase.getPurchaserId());
        detailVO.setPurchaserRealName(getUserRealName(purchase.getPurchaserId()));
        detailVO.setPurchaseConfirmTime(purchase.getPurchaseConfirmTime());
        detailVO.setRemark(purchase.getRemark());
        detailVO.setCreateTime(purchase.getCreateTime());
        return detailVO;
    }

    private PurchaseItemVO toItemVO(PurchaseItem purchaseItem) {
        PurchaseItemVO itemVO = new PurchaseItemVO();
        itemVO.setProductId(purchaseItem.getProductId());
        itemVO.setProductName(purchaseItem.getProductName());
        itemVO.setQuantity(purchaseItem.getQuantity());
        itemVO.setUnitPrice(purchaseItem.getUnitPrice());
        itemVO.setAmount(purchaseItem.getAmount());
        itemVO.setReceivedQuantity(purchaseItem.getReceivedQuantity());
        return itemVO;
    }

    private String getDepartmentName(Long deptId) {
        Department department = deptId == null ? null : departmentMapper.selectById(deptId);
        return department == null ? null : department.getDeptName();
    }

    private String getSupplierName(Long supplierId) {
        Supplier supplier = supplierId == null ? null : supplierMapper.selectById(supplierId);
        return supplier == null ? null : supplier.getSupplierName();
    }

    private String getUserRealName(Long userId) {
        User user = userId == null ? null : userMapper.selectById(userId);
        return user == null ? null : user.getRealName();
    }
}
