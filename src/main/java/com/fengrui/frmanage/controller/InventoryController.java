package com.fengrui.frmanage.controller;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.dto.inventory.InitInventoryDTO;
import com.fengrui.frmanage.dto.inventory.InventoryQueryDTO;
import com.fengrui.frmanage.dto.inventory.InventoryRecordQueryDTO;
import com.fengrui.frmanage.service.inventory.InventoryService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.inventory.InventoryRecordVO;
import com.fengrui.frmanage.vo.inventory.InventoryVO;
import com.fengrui.frmanage.vo.inventory.InventoryWarningVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存管理接口控制器。
 */
@Validated
@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "库存管理", description = "库存查询、库存预警、库存流水和库存初始化接口")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * 分页查询当前库存。
     *
     * @param queryDTO 查询参数
     * @return 当前库存分页数据
     */
    @GetMapping("/list")
    // TODO 当前 admin 模式不做接口权限拦截，后续补充 @PreAuthorize 库存查询权限。
    @Operation(summary = "库存查询", description = "分页查询启用商品的当前库存快照，未入库商品库存按0展示")
    public Result<PageResultVO<InventoryVO>> listInventory(@ModelAttribute InventoryQueryDTO queryDTO) {
        return Result.success(inventoryService.pageInventory(queryDTO));
    }

    /**
     * 分页查询库存预警。
     *
     * @param queryDTO 查询参数
     * @return 库存预警分页数据
     */
    @GetMapping("/warning")
    // TODO 当前 admin 模式不做接口权限拦截，后续补充 @PreAuthorize 库存预警查询权限。
    @Operation(summary = "库存预警查询", description = "分页查询库存低于预警阈值的启用商品，并返回建议采购量")
    public Result<PageResultVO<InventoryWarningVO>> warning(@ModelAttribute InventoryQueryDTO queryDTO) {
        return Result.success(inventoryService.pageWarning(queryDTO));
    }

    /**
     * 分页查询库存流水。
     *
     * @param queryDTO 查询参数
     * @return 库存流水分页数据
     */
    @GetMapping("/record")
    // TODO 当前 admin 模式不做接口权限拦截，后续补充 @PreAuthorize 库存流水审计权限。
    @Operation(summary = "库存流水查询", description = "分页查询库存变动流水，支持商品、类型、时间和关联单号筛选")
    public Result<PageResultVO<InventoryRecordVO>> record(@ModelAttribute InventoryRecordQueryDTO queryDTO) {
        return Result.success(inventoryService.pageRecord(queryDTO));
    }

    /**
     * 初始化库存。
     *
     * @param initInventoryDTO 初始化参数
     * @return 空响应
     */
    @PostMapping("/init")
    // TODO 库存初始化仅限系统管理员，后续接入 JWT 后补充 @PreAuthorize("hasRole('admin')")。
    @Operation(summary = "库存初始化", description = "系统上线期由管理员批量设置商品期初库存和成本单价")
    public Result<Void> init(@Valid @RequestBody InitInventoryDTO initInventoryDTO) {
        inventoryService.initInventory(initInventoryDTO);
        return Result.success("库存初始化完成", null);
    }
}
