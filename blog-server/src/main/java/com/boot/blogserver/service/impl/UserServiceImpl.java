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
import com.boot.blogserver.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    /**
     * 用户登录
     * @param userLoginDTO
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        User user = userMapper.getByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("该用户不存在");
        }
        if (UserStatusConstant.STATUS_DISABLED.equals(user.getStatus())) {
            throw new ForbiddenException("当前账号已被禁用");
        }
        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        return user;
    }

    /**
     * 用户注册
     * @param userRegisterDTO
     */
    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        String username = userRegisterDTO.getUsername();
        String email = userRegisterDTO.getEmail();

        if (userMapper.getByUsername(username) != null) {
            throw BusinessException.conflict("该用户名已存在");
        }
        if(userMapper.getByEmail(email) != null) {
            throw BusinessException.conflict("该邮箱已注册");
        }

        User user = new User();
        BeanUtils.copyProperties(userRegisterDTO, user);
        user.setPassword(PasswordUtil.encode(userRegisterDTO.getPassword()));
        user.setRole(RoleConstant.USER);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedTime(now);
        user.setUpdatedTime(now);

        userMapper.save(user);

        return user;
    }

    /**
     * 更新用户信息
     * @param userUpdateDTO
     */
    @Override
    public User updte(UserUpdateDTO userUpdateDTO) {
        if (!userUpdateDTO.getId().equals(BaseContext.getCurrentId())) {
            throw new ForbiddenException("无权修改其他用户资料");
        }
        User user = userMapper.getById(userUpdateDTO.getId());
        if (user == null) {
            throw BusinessException.notFound("该用户不存在");
        }
        if (userUpdateDTO.getEmail() != null) {
            User existingUser = userMapper.getByEmail(userUpdateDTO.getEmail());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw BusinessException.conflict("该邮箱已注册");
            }
        }

        // UserUpdateDTO 本轮不承担改密职责，只允许覆盖资料字段。
        BeanUtils.copyProperties(userUpdateDTO, user);
        user.setUpdatedTime(LocalDateTime.now());

        userMapper.update(user);
        return userMapper.getById(userUpdateDTO.getId());
    }

    @Override
    public User userInfo(Long id) {
        User user = userMapper.getById(id);
        if (user == null) {
            throw BusinessException.notFound("该用户不存在");
        }
        return user;
    }
}
