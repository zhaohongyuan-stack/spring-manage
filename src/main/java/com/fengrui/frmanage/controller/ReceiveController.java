package com.fengrui.frmanage.controller;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.dto.receive.AddReceiveDTO;
import com.fengrui.frmanage.dto.receive.ReceiveApproveDTO;
import com.fengrui.frmanage.dto.receive.ReceiveCancelDTO;
import com.fengrui.frmanage.dto.receive.ReceiveConfirmDTO;
import com.fengrui.frmanage.dto.receive.ReceiveListQueryDTO;
import com.fengrui.frmanage.service.receive.ReceiveService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.receive.ReceiveDetailVO;
import com.fengrui.frmanage.vo.receive.ReceiveListItemVO;
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
 * 领用出库接口控制器。
 */
@Validated
@RestController
@RequestMapping("/api/v1/receive")
@Tag(name = "领用出库管理", description = "领用单创建、查询、审批、出库和取消接口")
public class ReceiveController {

    private final ReceiveService receiveService;

    public ReceiveController(ReceiveService receiveService) {
        this.receiveService = receiveService;
    }

    /**
     * 创建领用申请。
     *
     * @param addReceiveDTO 创建领用申请参数
     * @return 领用单ID和单号
     */
    @PostMapping("/add")
    // TODO 当前 admin 模式不做接口权限拦截，后续补充部门员工权限注解。
    @Operation(summary = "创建领用申请", description = "员工发起物资领用申请并保存领用明细")
    public Result<Map<String, Object>> addReceive(@Valid @RequestBody AddReceiveDTO addReceiveDTO) {
        return Result.success("申请成功", receiveService.addReceive(addReceiveDTO));
    }

    /**
     * 分页查询领用单。
     *
     * @param queryDTO 查询参数
     * @return 领用单分页列表
     */
    @GetMapping("/list")
    // TODO 当前 admin 模式不做角色过滤，后续按部门负责人、仓库管理员、财务、总经理权限过滤。
    @Operation(summary = "领用单列表查询", description = "分页查询领用单，支持按状态、部门、申请人和时间范围筛选")
    public Result<PageResultVO<ReceiveListItemVO>> listReceive(@ModelAttribute ReceiveListQueryDTO queryDTO) {
        return Result.success(receiveService.pageList(queryDTO));
    }

    /**
     * 查询领用单详情。
     *
     * @param receiveId 领用单ID
     * @return 领用单详情
     */
    @GetMapping("/detail")
    // TODO 后续接入 JWT 后补充领用单详情访问权限注解。
    @Operation(summary = "领用单详情查询", description = "根据领用单ID查询领用单头和明细信息")
    public Result<ReceiveDetailVO> detail(
            @NotNull(message = "领用单ID不能为空") @RequestParam(required = false) Long receiveId) {
        return Result.success(receiveService.detail(receiveId));
    }

    /**
     * 审批领用单。
     *
     * @param approveDTO 审批参数
     * @return 空响应
     */
    @PostMapping("/approve")
    // TODO 当前保留部门负责人业务校验，后续补充接口权限注解。
    @Operation(summary = "领用单审批", description = "审批通过或驳回待审批领用单")
    public Result<Void> approve(@Valid @RequestBody ReceiveApproveDTO approveDTO) {
        receiveService.approve(approveDTO);
        return Result.success(Boolean.TRUE.equals(approveDTO.getApproved()) ? "审批通过" : "审批驳回", null);
    }

    /**
     * 确认出库。
     *
     * @param confirmDTO 出库确认参数
     * @return 空响应
     */
    @PostMapping("/confirm")
    // TODO 当前 admin 模式不做接口权限拦截，后续补充仓库管理员权限注解。
    @Operation(summary = "领用确认出库", description = "仓库管理员确认出库并扣减库存")
    public Result<Void> confirm(@Valid @RequestBody ReceiveConfirmDTO confirmDTO) {
        receiveService.confirm(confirmDTO);
        return Result.success("出库成功", null);
    }

    /**
     * 取消领用单。
     *
     * @param cancelDTO 取消参数
     * @return 空响应
     */
    @PostMapping("/cancel")
    // TODO 后续接入 JWT 后补充申请人取消权限校验。
    @Operation(summary = "取消领用单", description = "取消待审批或待出库的领用单")
    public Result<Void> cancel(@Valid @RequestBody ReceiveCancelDTO cancelDTO) {
        receiveService.cancel(cancelDTO);
        return Result.success("已取消", null);
    }
}
