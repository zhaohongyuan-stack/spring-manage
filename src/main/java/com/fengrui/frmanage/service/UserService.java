package com.fengrui.frmanage.service;

import com.fengrui.frmanage.dto.AddUserDTO;
import com.fengrui.frmanage.dto.UserListQueryDTO;
import com.fengrui.frmanage.vo.AddUserVO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.UserListVO;

/**
 * 用户业务服务。
 */
public interface UserService {

    /**
     * 新增系统用户。
     *
     * @param addUserDTO 新增用户参数
     * @return 新增用户ID
     */
    AddUserVO addUser(AddUserDTO addUserDTO);

    /**
     * 分页查询用户列表。
     *
     * @param queryDTO 查询参数
     * @return 用户分页数据
     */
    PageResultVO<UserListVO> listUsers(UserListQueryDTO queryDTO);
}
