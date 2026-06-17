package com.fengrui.frmanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.frmanage.common.enums.RoleEnum;
import com.fengrui.frmanage.dto.AddUserDTO;
import com.fengrui.frmanage.dto.DeleteUserDTO;
import com.fengrui.frmanage.dto.UserListQueryDTO;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.DepartmentMapper;
import com.fengrui.frmanage.mapper.UserMapper;
import com.fengrui.frmanage.service.UserService;
import com.fengrui.frmanage.vo.AddUserVO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.UserListVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户业务服务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    private static final short ENABLED_STATUS = 1;

    private static final short DISABLED_STATUS = 0;

    private final UserMapper userMapper;

    private final DepartmentMapper departmentMapper;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper,
                           DepartmentMapper departmentMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 新增系统用户。
     *
     * @param addUserDTO 新增用户参数
     * @return 新增用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddUserVO addUser(AddUserDTO addUserDTO) {
        validateAddUser(addUserDTO);

        User user = new User();
        user.setUsername(addUserDTO.getUsername());
        user.setPassword(passwordEncoder.encode(addUserDTO.getPassword()));
        user.setRealName(addUserDTO.getRealName());
        user.setRole(addUserDTO.getRole());
        user.setDeptId(resolveDeptId(addUserDTO));
        user.setPhone(addUserDTO.getPhone());
        user.setStatus(ENABLED_STATUS);
        userMapper.insert(user);
        return new AddUserVO(user.getId());
    }

    /**
     * 分页查询用户列表。
     *
     * @param queryDTO 查询参数
     * @return 用户分页数据
     */
    @Override
    public PageResultVO<UserListVO> listUsers(UserListQueryDTO queryDTO) {
        IPage<UserListVO> page = userMapper.selectUserPage(
                Page.of(queryDTO.getPageNum(), queryDTO.getPageSize()),
                queryDTO
        );
        page.getRecords().forEach(user -> user.setRoleName(RoleEnum.getNameByCode(user.getRole())));
        return new PageResultVO<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 删除系统用户（逻辑禁用）。
     *
     * @param deleteUserDTO 删除用户参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(DeleteUserDTO deleteUserDTO) {
        User existsUser = getExistingUser(deleteUserDTO.getUserId());
        User user = new User();
        user.setId(existsUser.getId());
        user.setStatus(DISABLED_STATUS);
        userMapper.updateById(user);
    }

    /**
     * 获取已存在的启用用户。
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    private User getExistingUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || DISABLED_STATUS == user.getStatus()) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 校验新增用户业务规则。
     *
     * @param addUserDTO 新增用户参数
     */
    private void validateAddUser(AddUserDTO addUserDTO) {
        RoleEnum roleEnum = RoleEnum.getByCode(addUserDTO.getRole());
        if (roleEnum == null) {
            throw new BusinessException("角色编码不存在");
        }

        Long sameUsernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, addUserDTO.getUsername())
        );
        if (sameUsernameCount > 0) {
            throw new BusinessException("登录账号已存在");
        }

        if (requiresDepartment(roleEnum)) {
            if (addUserDTO.getDeptId() == null) {
                throw new BusinessException("部门员工或部门负责人必须指定所属部门");
            }
            if (departmentMapper.selectById(addUserDTO.getDeptId()) == null) {
                throw new BusinessException("所属部门不存在");
            }
            return;
        }

        if (addUserDTO.getDeptId() != null && departmentMapper.selectById(addUserDTO.getDeptId()) == null) {
            throw new BusinessException("所属部门不存在");
        }
    }

    /**
     * 解析入库部门ID：职能类角色不绑定部门。
     *
     * @param addUserDTO 新增用户参数
     * @return 部门ID，职能类角色返回 null
     */
    private Long resolveDeptId(AddUserDTO addUserDTO) {
        RoleEnum roleEnum = RoleEnum.getByCode(addUserDTO.getRole());
        if (roleEnum == null || !requiresDepartment(roleEnum)) {
            return null;
        }
        return addUserDTO.getDeptId();
    }

    /**
     * 判断角色是否必须绑定部门。
     *
     * @param roleEnum 角色枚举
     * @return 是否必须指定部门
     */
    private boolean requiresDepartment(RoleEnum roleEnum) {
        return RoleEnum.DEPT_EMPLOYEE == roleEnum || RoleEnum.DEPT_HEAD == roleEnum;
    }
}
