package com.boot.blogserver.service.impl;

import com.blog.constant.RoleConstant;
import com.blog.constant.UserStatusConstant;
import com.blog.context.BaseContext;
import com.blog.dto.UserLoginDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.dto.UserUpdateDTO;
import com.blog.entry.User;
import com.blog.exception.BusinessException;
import com.blog.exception.ForbiddenException;
import com.blog.utils.PasswordUtil;
import com.boot.blogserver.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    @Test
    void registerShouldPersistEncodedPassword() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("alice");
        dto.setPassword("plain-password");
        dto.setEmail("alice@example.com");
        dto.setNickname("Alice");

        when(userMapper.getByUsername("alice")).thenReturn(null);
        when(userMapper.getByEmail("alice@example.com")).thenReturn(null);

        userService.register(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("alice", saved.getUsername());
        assertEquals("alice@example.com", saved.getEmail());
        assertEquals(RoleConstant.USER, saved.getRole());
        assertNotNull(saved.getCreatedTime());
        assertNotNull(saved.getUpdatedTime());
        assertFalse("plain-password".equals(saved.getPassword()));
        assertTrue(PasswordUtil.matches("plain-password", saved.getPassword()));
    }

    @Test
    void loginShouldUsePasswordHashMatching() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("alice");
        dto.setPassword("plain-password");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setStatus(UserStatusConstant.STATUS_NORMAL);
        user.setPassword(PasswordUtil.encode("plain-password"));
        when(userMapper.getByUsername("alice")).thenReturn(user);

        User loginUser = userService.login(dto);

        assertSame(user, loginUser);
    }

    @Test
    void loginShouldRejectWrongPassword() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("alice");
        dto.setPassword("wrong-password");

        User user = new User();
        user.setUsername("alice");
        user.setStatus(UserStatusConstant.STATUS_NORMAL);
        user.setPassword(PasswordUtil.encode("plain-password"));
        when(userMapper.getByUsername("alice")).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.login(dto));

        assertEquals("密码错误", exception.getMessage());
    }

    @Test
    void updateShouldKeepExistingPassword() {
        BaseContext.setCurrentId(1L);

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1L);
        dto.setEmail("new@example.com");
        dto.setNickname("New Nick");
        dto.setAvatarUrl("avatar");
        dto.setBio("bio");

        String encodedPassword = PasswordUtil.encode("plain-password");
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("alice");
        existingUser.setPassword(encodedPassword);
        existingUser.setEmail("old@example.com");
        existingUser.setUpdatedTime(LocalDateTime.now().minusDays(1));

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("alice");
        updatedUser.setPassword(encodedPassword);
        updatedUser.setEmail("new@example.com");

        when(userMapper.getById(1L)).thenReturn(existingUser).thenReturn(updatedUser);
        when(userMapper.getByEmail("new@example.com")).thenReturn(null);

        User result = userService.updte(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).update(userCaptor.capture());
        User submitted = userCaptor.getValue();
        assertEquals(encodedPassword, submitted.getPassword());
        assertEquals("new@example.com", submitted.getEmail());
        assertEquals("New Nick", submitted.getNickname());
        assertNotNull(submitted.getUpdatedTime());
        assertSame(updatedUser, result);
    }

    @Test
    void loginShouldRejectDisabledUser() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("alice");
        dto.setPassword("plain-password");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setStatus(UserStatusConstant.STATUS_DISABLED);
        user.setPassword(PasswordUtil.encode("plain-password"));
        when(userMapper.getByUsername("alice")).thenReturn(user);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> userService.login(dto));

        assertEquals("当前账号已被禁用", exception.getMessage());
    }

    @Test
    void updateShouldRejectUpdatingOtherUser() {
        BaseContext.setCurrentId(2L);

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1L);
        dto.setEmail("new@example.com");

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> userService.updte(dto));

        assertEquals("无权修改其他用户资料", exception.getMessage());
        verify(userMapper, never()).getById(1L);
    }

    @Test
    void registerShouldRejectDuplicateUsernameBeforeSave() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("alice");
        dto.setPassword("plain-password");
        dto.setEmail("alice@example.com");

        when(userMapper.getByUsername("alice")).thenReturn(new User());

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.register(dto));

        assertEquals("该用户名已存在", exception.getMessage());
        verify(userMapper, never()).save(any(User.class));
        verify(userMapper, never()).getByEmail(eq("alice@example.com"));
    }
}
