package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fengrui.frmanage.dto.UserListQueryDTO;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.vo.UserListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户持久层接口。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户列表。
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 用户分页列表
     */
    @Select("""
            <script>
            SELECT
                u.id AS user_id,
                u.username AS account,
                u.real_name,
                u.role,
                u.dept_id,
                d.dept_name,
                u.phone,
                u.status,
                u.create_time
            FROM "user" u
            LEFT JOIN department d ON u.dept_id = d.id
            <where>
                <if test="query.realName != null and query.realName != ''">
                    AND u.real_name LIKE CONCAT('%', #{query.realName}, '%')
                </if>
                <if test="query.role != null and query.role != ''">
                    AND u.role = #{query.role}
                </if>
                <if test="query.deptId != null">
                    AND u.dept_id = #{query.deptId}
                </if>
            </where>
            ORDER BY u.id DESC
            </script>
            """)
    IPage<UserListVO> selectUserPage(IPage<UserListVO> page, @Param("query") UserListQueryDTO query);
}
