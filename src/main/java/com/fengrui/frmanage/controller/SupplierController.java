package com.fengrui.frmanage.controller;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.dto.supplier.AddSupplierDTO;
import com.fengrui.frmanage.dto.supplier.DeleteSupplierDTO;
import com.fengrui.frmanage.dto.supplier.SupplierQueryDTO;
import com.fengrui.frmanage.service.supplier.SupplierService;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.supplier.SupplierVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商接口控制器。
 */
@RestController
@RequestMapping("/api/v1/supplier")
@Tag(name = "供应商管理", description = "供应商新增、删除和分页查询接口")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * 新增供应商。
     *
     * @param addSupplierDTO 新增供应商参数
     * @return 供应商ID
     */
    @PostMapping("/add")
    // TODO 后续补充 @PreAuthorize("hasAnyRole('admin','purchaser')")
    @Operation(summary = "新增供应商", description = "采购员或系统管理员录入合作供应商信息")
    public Result<Long> addSupplier(@Valid @RequestBody AddSupplierDTO addSupplierDTO) {
        return Result.success("新增成功", supplierService.addSupplier(addSupplierDTO));
    }

    /**
     * 删除供应商。
     *
     * @param deleteSupplierDTO 删除供应商参数
     * @return 空响应
     */
    @PostMapping("/delete")
    // TODO 后续补充 @PreAuthorize("hasAnyRole('admin','purchaser')")
    @Operation(summary = "删除供应商", description = "根据供应商ID删除供应商")
    public Result<Void> deleteSupplier(@Valid @RequestBody DeleteSupplierDTO deleteSupplierDTO) {
        supplierService.deleteSupplier(deleteSupplierDTO);
        return Result.success("删除成功", null);
    }

    /**
     * 分页查询供应商。
     *
     * @param supplierQueryDTO 查询参数
     * @return 供应商分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "供应商列表查询", description = "分页查询供应商，支持按供应商名称模糊查询")
    public Result<PageResultVO<SupplierVO>> listSupplier(@ModelAttribute SupplierQueryDTO supplierQueryDTO) {
        return Result.success(supplierService.listSupplier(supplierQueryDTO));
    }
}
