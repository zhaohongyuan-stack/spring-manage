package com.fengrui.frmanage.service.impl.department;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fengrui.frmanage.common.enums.DeptStatusEnum;
import com.fengrui.frmanage.dto.AddDepartmentDTO;
import com.fengrui.frmanage.entity.Department;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.DepartmentMapper;
import com.fengrui.frmanage.service.department.DepartmentService;
import com.fengrui.frmanage.vo.DepartmentTreeVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门业务服务实现。
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Long ROOT_PARENT_ID = 0L;

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    /**
     * 新增部门。
     *
     * @param addDepartmentDTO 新增部门参数
     * @return 部门ID
     */
    @Override
    public Long addDepartment(AddDepartmentDTO addDepartmentDTO) {
        validateDepartment(addDepartmentDTO);

        Department department = new Department();
        department.setDeptName(addDepartmentDTO.getDeptName());
        department.setParentId(addDepartmentDTO.getParentId());
        department.setStatus(DeptStatusEnum.ENABLED.getCode().shortValue());
        departmentMapper.insert(department);
        return department.getId();
    }

    /**
     * 查询部门树。
     *
     * @return 部门树形列表
     */
    @Override
    public List<DepartmentTreeVO> getDepartmentTree() {
        List<Department> departments = departmentMapper.selectList(
                new LambdaQueryWrapper<Department>().orderByAsc(Department::getId)
        );
        return buildDepartmentTree(departments);
    }

    /**
     * 校验新增部门业务规则。
     *
     * @param addDepartmentDTO 新增部门参数
     */
    private void validateDepartment(AddDepartmentDTO addDepartmentDTO) {
        Long sameNameCount = departmentMapper.selectCount(
                new LambdaQueryWrapper<Department>().eq(Department::getDeptName, addDepartmentDTO.getDeptName())
        );
        if (sameNameCount > 0) {
            throw new BusinessException("部门名称已存在");
        }

        Long parentId = addDepartmentDTO.getParentId();
        if (!ROOT_PARENT_ID.equals(parentId) && departmentMapper.selectById(parentId) == null) {
            throw new BusinessException("上级部门不存在");
        }
    }

    /**
     * 将部门列表组装为树形结构。
     *
     * @param departments 部门列表
     * @return 部门树
     */
    private List<DepartmentTreeVO> buildDepartmentTree(List<Department> departments) {
        Map<Long, DepartmentTreeVO> departmentMap = new LinkedHashMap<>();
        for (Department department : departments) {
            DepartmentTreeVO treeNode = toTreeVO(department);
            departmentMap.put(treeNode.getId(), treeNode);
        }

        List<DepartmentTreeVO> roots = new ArrayList<>();
        for (DepartmentTreeVO treeNode : departmentMap.values()) {
            DepartmentTreeVO parent = departmentMap.get(treeNode.getParentId());
            if (parent == null) {
                roots.add(treeNode);
                continue;
            }
            parent.getChildren().add(treeNode);
        }
        sortTree(roots);
        return roots;
    }

    /**
     * 转换部门树节点。
     *
     * @param department 部门实体
     * @return 部门树节点
     */
    private DepartmentTreeVO toTreeVO(Department department) {
        DepartmentTreeVO treeNode = new DepartmentTreeVO();
        treeNode.setId(department.getId());
        treeNode.setDeptName(department.getDeptName());
        treeNode.setParentId(department.getParentId());
        treeNode.setChildren(new ArrayList<>());
        return treeNode;
    }

    /**
     * 按部门ID排序树节点。
     *
     * @param treeNodes 树节点列表
     */
    private void sortTree(List<DepartmentTreeVO> treeNodes) {
        treeNodes.sort(Comparator.comparing(DepartmentTreeVO::getId));
        treeNodes.forEach(node -> sortTree(node.getChildren()));
    }
}
