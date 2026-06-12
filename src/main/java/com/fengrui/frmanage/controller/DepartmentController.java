package com.fengrui.frmanage.controller;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.dto.AddDepartmentDTO;
import com.fengrui.frmanage.service.department.DepartmentService;
import com.fengrui.frmanage.vo.DepartmentTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 部门接口控制器。
 */
@RestController
@RequestMapping("/api/v1/department")
@Tag(name = "部门管理", description = "部门新增和树形查询接口")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * 新增部门。
     *
     * @param addDepartmentDTO 新增部门参数
     * @return 新增部门ID
     */
    @PostMapping("/add")
    // TODO 后续补充 @PreAuthorize("hasRole('admin')")
    @Operation(summary = "新增部门", description = "系统管理员添加酒店部门，支持通过 parentId 建立层级")
    public Result<Map<String, Long>> addDepartment(@Valid @RequestBody AddDepartmentDTO addDepartmentDTO) {
        Long departmentId = departmentService.addDepartment(addDepartmentDTO);
        return Result.success("新增成功", Map.of("id", departmentId));
    }

    /**
     * 查询部门树。
     *
     * @return 部门树形列表
     */
    @GetMapping("/tree")
    @Operation(summary = "部门列表查询", description = "查询所有部门并以树形结构返回")
    public Result<List<DepartmentTreeVO>> getDepartmentTree() {
        return Result.success(departmentService.getDepartmentTree());
    }
}
