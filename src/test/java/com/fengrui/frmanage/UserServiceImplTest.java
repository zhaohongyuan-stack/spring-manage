package com.fengrui.frmanage;

import com.fengrui.frmanage.dto.AddUserDTO;
import com.fengrui.frmanage.entity.Department;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.DepartmentMapper;
import com.fengrui.frmanage.mapper.UserMapper;
import com.fengrui.frmanage.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户业务服务测试。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        when(userMapper.selectCount(any())).thenReturn(0L);
    }

    @Test
    void addUserShouldAllowPurchaserWithoutDept() {
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(102L);
            return 1;
        });
        AddUserDTO addUserDTO = buildAddUserDTO("purchaser", null);

        userService.addUser(addUserDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertNull(userCaptor.getValue().getDeptId());
    }

    @Test
    void addUserShouldIgnoreDeptForPurchaserEvenWhenProvided() {
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(departmentMapper.selectById(1L)).thenReturn(new Department());
        AddUserDTO addUserDTO = buildAddUserDTO("purchaser", 1L);

        userService.addUser(addUserDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertNull(userCaptor.getValue().getDeptId());
    }

    @Test
    void addUserShouldRequireDeptForDeptEmployee() {
        AddUserDTO addUserDTO = buildAddUserDTO("dept_employee", null);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.addUser(addUserDTO));
        assertEquals("部门员工或部门负责人必须指定所属部门", exception.getMessage());
    }

    @Test
    void addUserShouldPersistDeptForDeptHead() {
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(departmentMapper.selectById(1L)).thenReturn(new Department());
        AddUserDTO addUserDTO = buildAddUserDTO("dept_head", 1L);

        userService.addUser(addUserDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertEquals(1L, userCaptor.getValue().getDeptId());
    }

    private AddUserDTO buildAddUserDTO(String role, Long deptId) {
        AddUserDTO addUserDTO = new AddUserDTO();
        addUserDTO.setUsername("test_user");
        addUserDTO.setPassword("123456");
        addUserDTO.setRealName("测试用户");
        addUserDTO.setRole(role);
        addUserDTO.setDeptId(deptId);
        return addUserDTO;
    }
}
