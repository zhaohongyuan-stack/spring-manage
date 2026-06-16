package com.fengrui.frmanage.controller;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.dto.purchase.AddPurchaseDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseApproveDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseCancelDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseConfirmDTO;
import com.fengrui.frmanage.dto.purchase.PurchaseListQueryDTO;
import com.fengrui.frmanage.service.purchase.PurchaseService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.purchase.PurchaseDetailVO;
import com.fengrui.frmanage.vo.purchase.PurchaseListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 采购接口控制器。
 */
@Validated
@RestController
@RequestMapping("/api/v1/purchase")
@Tag(name = "采购管理", description = "采购单创建、查询、审批、确认和取消接口")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * 创建采购申请。
     *
     * @param addPurchaseDTO 创建采购申请参数
     * @return 采购单ID和单号
     */
    @PostMapping("/add")
    // TODO 后续接入 JWT 后补充采购申请权限注解。
    @Operation(summary = "创建采购申请", description = "创建采购单并保存采购明细")
    public Result<Map<String, Object>> addPurchase(@Valid @RequestBody AddPurchaseDTO addPurchaseDTO) {
        return Result.success("申请成功", purchaseService.addPurchase(addPurchaseDTO));
    }

    /**
     * 分页查询采购单。
     *
     * @param queryDTO 查询参数
     * @return 采购单分页列表
     */
    @GetMapping("/list")
    // TODO 本阶段默认 admin 口径不做角色过滤，后续根据 operatorUserId 解析角色后收紧查询范围。
    @Operation(summary = "采购单列表查询", description = "分页查询采购单，支持状态和单号筛选")
    public Result<PageResultVO<PurchaseListItemVO>> listPurchase(@ModelAttribute PurchaseListQueryDTO queryDTO) {
        return Result.success(purchaseService.pageList(queryDTO));
    }

    /**
     * 查询采购单详情。
     *
     * @param purchaseId 采购单ID
     * @return 采购单详情
     */
    @GetMapping("/detail")
    // TODO 后续接入 JWT 后补充采购单详情访问权限注解。
    @Operation(summary = "采购单详情查询", description = "根据采购单ID查询采购单头和明细信息")
    public Result<PurchaseDetailVO> detail(
            @NotNull(message = "采购单ID不能为空") @RequestParam(required = false) Long purchaseId) {
        return Result.success(purchaseService.detail(purchaseId));
    }

    /**
     * 审批采购单。
     *
     * @param approveDTO 审批参数
     * @return 空响应
     */
    @PostMapping("/approve")
    // TODO 本阶段不校验审批人角色，后续补充 @PreAuthorize 和部门负责人校验。
    @Operation(summary = "采购单审批", description = "审批通过或驳回待审批采购单")
    public Result<Void> approve(@Valid @RequestBody PurchaseApproveDTO approveDTO) {
        purchaseService.approve(approveDTO);
        return Result.success(Boolean.TRUE.equals(approveDTO.getApproved()) ? "审批通过" : "审批驳回", null);
    }

    /**
     * 确认采购。
     *
     * @param confirmDTO 确认采购参数
     * @return 空响应
     */
    @PostMapping("/confirm")
    // TODO 本阶段不校验采购员角色，后续补充 @PreAuthorize("hasAnyRole('admin','purchaser')")。
    @Operation(summary = "采购确认", description = "将已通过采购单确认为采购中")
    public Result<Void> confirm(@Valid @RequestBody PurchaseConfirmDTO confirmDTO) {
        purchaseService.confirm(confirmDTO);
        return Result.success("已确认为采购中", null);
    }

    /**
     * 取消采购单。
     *
     * @param cancelDTO 取消参数
     * @return 空响应
     */
    @PostMapping("/cancel")
    // TODO 本阶段不校验操作人角色，后续补充申请人本人或采购员权限校验。
    @Operation(summary = "取消采购单", description = "取消未关联入库单且处于可取消状态的采购单")
    public Result<Void> cancel(@Valid @RequestBody PurchaseCancelDTO cancelDTO) {
        purchaseService.cancel(cancelDTO);
        return Result.success("已取消", null);
    }
}
