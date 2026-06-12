package com.fengrui.frmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengrui.frmanage.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门持久层接口。
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
