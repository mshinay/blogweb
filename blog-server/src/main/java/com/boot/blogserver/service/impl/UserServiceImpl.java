package com.boot.blogserver.service.impl;

import com.blog.constant.RoleConstant;
import com.blog.dto.UserLoginDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.dto.UserUpdateDTO;
import com.blog.entry.User;
import com.boot.blogserver.mapper.UserMapper;
import com.boot.blogserver.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
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
        //从数据库里获得用户信息
        User user = userMapper.getByUsername(username);
        //进行条件判断
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
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

        //防止重复注册
        if (userMapper.getByUsername(username) != null) {
            throw new RuntimeException("该用户名已存在");
        }
        if(userMapper.getByEmail(email) != null) {
            throw new RuntimeException("该邮箱已注册");
        }

        User user = new User();
        BeanUtils.copyProperties(userRegisterDTO, user);
        user.setRole(RoleConstant.USER);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedTime(now);
        user.setUpdatedTime(now);

        //写入数据库
        userMapper.save(user);

        return user;
    }

    /**
     * 更新用户信息
     * @param userUpdateDTO
     */
    @Override
    public User updte(UserUpdateDTO userUpdateDTO) {
        User user = userMapper.getById(userUpdateDTO.getId());
        log.info("{}",user);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        if(userUpdateDTO.getEmail()!=null&&userMapper.getByEmail(userUpdateDTO.getEmail()) != null) {
            throw new RuntimeException("该邮箱已注册");
        }

        BeanUtils.copyProperties(userUpdateDTO, user);
        user.setUpdatedTime(LocalDateTime.now());

        userMapper.update(user);
        return userMapper.getById(userUpdateDTO.getId());
    }

    @Override
    public User userInfo(Long id) {
        User user = userMapper.getById(id);
        log.info("{}",user);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        return user;
    }
}
