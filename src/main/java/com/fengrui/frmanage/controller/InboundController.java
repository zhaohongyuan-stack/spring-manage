package com.fengrui.frmanage.controller;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.dto.inbound.AddInboundDTO;
import com.fengrui.frmanage.dto.inbound.InboundConfirmDTO;
import com.fengrui.frmanage.dto.inbound.InboundListQueryDTO;
import com.fengrui.frmanage.service.inbound.InboundService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.inbound.InboundDetailVO;
import com.fengrui.frmanage.vo.inbound.InboundListItemVO;
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
 * 入库接口控制器。
 */
@Validated
@RestController
@RequestMapping("/api/v1/inbound")
@Tag(name = "入库管理", description = "入库单创建、查询和验收接口")
public class InboundController {

    private final InboundService inboundService;

    public InboundController(InboundService inboundService) {
        this.inboundService = inboundService;
    }

    /**
     * 创建入库单。
     *
     * @param addInboundDTO 创建入库单参数
     * @return 入库单ID和单号
     */
    @PostMapping("/add")
    // TODO 后续接入 JWT 后补充仓库管理员权限注解。
    @Operation(summary = "创建入库单", description = "根据采购单创建入库单，等待部门负责人验收")
    public Result<Map<String, Object>> addInbound(@Valid @RequestBody AddInboundDTO addInboundDTO) {
        return Result.success("入库单创建成功，等待验收", inboundService.addInbound(addInboundDTO));
    }

    /**
     * 分页查询入库单。
     *
     * @param queryDTO 查询参数
     * @return 入库单分页列表
     */
    @GetMapping("/list")
    // TODO 当前 admin 模式不做角色过滤，后续按仓库管理员、部门负责人、财务、总经理权限过滤。
    @Operation(summary = "入库单列表查询", description = "分页查询入库单，支持按类型、状态、采购单号筛选")
    public Result<PageResultVO<InboundListItemVO>> listInbound(@ModelAttribute InboundListQueryDTO queryDTO) {
        return Result.success(inboundService.pageList(queryDTO));
    }

    /**
     * 查询入库单详情。
     *
     * @param inboundId 入库单ID
     * @return 入库单详情
     */
    @GetMapping("/detail")
    // TODO 后续接入 JWT 后补充入库单详情访问权限注解。
    @Operation(summary = "入库单详情查询", description = "根据入库单ID查询入库单头和明细信息")
    public Result<InboundDetailVO> detail(
            @NotNull(message = "入库单ID不能为空") @RequestParam(required = false) Long inboundId) {
        return Result.success(inboundService.detail(inboundId));
    }

    /**
     * 入库单验收确认。
     *
     * @param confirmDTO 验收确认参数
     * @return 空响应
     */
    @PostMapping("/confirm")
    // TODO 本阶段保留部门负责人业务校验，后续接入 JWT 后补充接口权限注解。
    @Operation(summary = "入库单验收确认", description = "验收入库单并更新库存、库存流水和采购单收货进度")
    public Result<Void> confirm(@Valid @RequestBody InboundConfirmDTO confirmDTO) {
        inboundService.confirm(confirmDTO);
        return Result.success("验收成功，库存已更新", null);
    }
}
