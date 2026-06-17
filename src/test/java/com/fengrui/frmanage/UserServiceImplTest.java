package com.fengrui.frmanage;

import com.fengrui.frmanage.dto.DeleteUserDTO;
import com.fengrui.frmanage.entity.User;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.mapper.UserMapper;
import com.fengrui.frmanage.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, passwordEncoder);
    }

    @Test
    void deleteUserShouldDisableExistingUser() {
        User existsUser = new User();
        existsUser.setId(101L);
        existsUser.setStatus((short) 1);
        when(userMapper.selectById(101L)).thenReturn(existsUser);

        DeleteUserDTO deleteUserDTO = new DeleteUserDTO();
        deleteUserDTO.setUserId(101L);
        userService.deleteUser(deleteUserDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        assertEquals(101L, userCaptor.getValue().getId());
        assertEquals((short) 0, userCaptor.getValue().getStatus());
    }

    @Test
    void deleteUserShouldThrowWhenUserNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        DeleteUserDTO deleteUserDTO = new DeleteUserDTO();
        deleteUserDTO.setUserId(999L);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.deleteUser(deleteUserDTO));
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void deleteUserShouldThrowWhenUserAlreadyDisabled() {
        User disabledUser = new User();
        disabledUser.setId(101L);
        disabledUser.setStatus((short) 0);
        when(userMapper.selectById(101L)).thenReturn(disabledUser);

        DeleteUserDTO deleteUserDTO = new DeleteUserDTO();
        deleteUserDTO.setUserId(101L);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.deleteUser(deleteUserDTO));
        assertEquals("用户不存在", exception.getMessage());
    }
}
