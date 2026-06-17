package com.fengrui.frmanage.controller;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.dto.AddUserDTO;
import com.fengrui.frmanage.dto.DeleteUserDTO;
import com.fengrui.frmanage.dto.UserListQueryDTO;
import com.fengrui.frmanage.service.UserService;
import com.fengrui.frmanage.vo.AddUserVO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.UserListVO;
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
 * 用户接口控制器。
 */
@Validated
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理", description = "系统用户新增、删除与查询接口")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 创建系统用户。
     *
     * @param addUserDTO 新增用户参数
     * @return 新增用户ID
     */
    @PostMapping("/add")
    @Operation(summary = "新增用户", description = "创建系统用户，分配角色和所属部门")
    public Result<AddUserVO> addUser(@Valid @RequestBody AddUserDTO addUserDTO) {
        return Result.success("新增成功", userService.addUser(addUserDTO));
    }

    /**
     * 分页查询用户列表。
     *
     * @param queryDTO 查询参数
     * @return 用户分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "用户列表查询", description = "分页查询用户，支持按姓名、角色、部门筛选")
    public Result<PageResultVO<UserListVO>> listUsers(@Valid @ModelAttribute UserListQueryDTO queryDTO) {
        return Result.success(userService.listUsers(queryDTO));
    }

    /**
     * 删除系统用户。
     *
     * @param deleteUserDTO 删除用户参数
     * @return 空响应
     */
    @PostMapping("/delete")
    // TODO 后续补充 @PreAuthorize("hasRole('admin')")
    @Operation(summary = "删除用户", description = "根据用户ID逻辑禁用用户")
    public Result<Void> deleteUser(@Valid @RequestBody DeleteUserDTO deleteUserDTO) {
        userService.deleteUser(deleteUserDTO);
        return Result.success("删除成功", null);
    }
}
