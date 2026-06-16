package com.fengrui.frmanage.service.purchase;

import com.fengrui.frmanage.dto.purchase.AddPurchaseDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseApproveDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseCancelDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseConfirmDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseListQueryDTO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.purchase.PurchaseDetailVO;
import com.fengrui.frmanage.vo.purchase.PurchaseListItemVO;

import java.util.Map;

/**
 * 采购业务服务。
 */
public interface PurchaseService {

    /**
     * 新增采购单。
     *
     * @param addPurchaseDTO 新增采购单参数
     * @return 采购单ID和单号
     */
    Map<String, Object> addPurchase(AddPurchaseDTO addPurchaseDTO);

    /**
     * 分页查询采购单。
     *
     * @param queryDTO 查询参数
     * @return 采购单分页列表
     */
    PageResultVO<PurchaseListItemVO> pageList(PurchaseListQueryDTO queryDTO);

    /**
     * 查询采购单详情。
     *
     * @param purchaseId 采购单ID
     * @return 采购单详情
     */
    PurchaseDetailVO detail(Long purchaseId);

    /**
     * 审批采购单。
     *
     * @param approveDTO 审批参数
     */
    void approve(PurchaseApproveDTO approveDTO);

    /**
     * 确认采购。
     *
     * @param confirmDTO 采购确认参数
     */
    void confirm(PurchaseConfirmDTO confirmDTO);

    /**
     * 取消采购单。
     *
     * @param cancelDTO 取消参数
     */
    void cancel(PurchaseCancelDTO cancelDTO);
}
