package com.fengrui.frmanage.service.department;

import com.fengrui.frmanage.dto.AddDepartmentDTO;
import com.fengrui.frmanage.vo.DepartmentTreeVO;

import java.util.List;

/**
 * 部门业务服务。
 */
public interface DepartmentService {

    /**
     * 新增部门。
     *
     * @param addDepartmentDTO 新增部门参数
     * @return 部门ID
     */
    Long addDepartment(AddDepartmentDTO addDepartmentDTO);

    /**
     * 查询部门树。
     *
     * @return 部门树形列表
     */
    List<DepartmentTreeVO> getDepartmentTree();
}
