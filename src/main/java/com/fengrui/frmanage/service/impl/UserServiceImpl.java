package com.fengrui.frmanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.frmanage.dto.AddUserDTO;
import com.fengrui.frmanage.dto.UserListQueryDTO;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.mapper.UserMapper;
import com.fengrui.frmanage.service.UserService;
import com.fengrui.frmanage.vo.AddUserVO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.UserListVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户业务服务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    private static final short ENABLED_STATUS = 1;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
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
        user.setDeptId(addUserDTO.getDeptId());
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
        return new PageResultVO<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 校验新增用户业务规则。
     *
     * @param addUserDTO 新增用户参数
     */
    private void validateAddUser(AddUserDTO addUserDTO) {
        Long sameUsernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, addUserDTO.getUsername())
        );
        if (sameUsernameCount > 0) {
            throw new IllegalArgumentException("登录账号已存在");
        }

        if (requiresDepartment(addUserDTO.getRole()) && addUserDTO.getDeptId() == null) {
            throw new IllegalArgumentException("部门员工或部门负责人必须指定所属部门");
        }
    }

    /**
     * 判断角色是否必须绑定部门。
     *
     * @param role 角色编码
     * @return 是否必须指定部门
     */
    private boolean requiresDepartment(String role) {
        return StringUtils.hasText(role) && ("dept_employee".equals(role) || "dept_head".equals(role));
    }
}
