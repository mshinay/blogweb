package com.boot.blogserver.service;
import com.blog.dto.UserLoginDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.dto.UserUpdateDTO;
import com.blog.entry.User;


public interface UserService {

    /**
     * 用户登录
     * @param userLoginDTO
     */
    User login(UserLoginDTO userLoginDTO);

    /**
     * 用户注册
     * @param userRegisterDTO
     */
    User register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户更新信息
     * @param userUpdateDTO
     */
    User updte(UserUpdateDTO userUpdateDTO);

    User userInfo(Long id);
}
