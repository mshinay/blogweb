package com.boot.blogserver.controller;

import com.blog.dto.UserLoginDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.dto.UserUpdateDTO;
import com.blog.entry.User;
import com.blog.properties.JwtProperties;
import com.blog.utils.JwtUtil;
import com.blog.vo.UserLoginVO;
import com.boot.blogserver.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.blog.result.Result;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private  UserService userService;
    @Resource
    private JwtProperties jwtProperties;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录{}",userLoginDTO);
        User user= userService.login(userLoginDTO);
        //登录成功后，生成jwt
        Map<String, Object> claims=new HashMap<>();
        claims.put("userId",user.getId());
        claims.put("username",user.getUsername());
        claims.put("avatarUrl",user.getAvatarUrl());
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user,userLoginVO);
        userLoginVO.setJwtToken(jwt);

        return Result.success(userLoginVO);
    }

    /**
     * 用户注册
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("/register")
    public Result<UserLoginVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册{}",userRegisterDTO);
        User user = userService.register(userRegisterDTO);

        //登录成功后，生成jwt
        Map<String, Object> claims=new HashMap<>();
        claims.put("userId",user.getId());
        claims.put("username",user.getUsername());
        claims.put("avatarUrl",user.getAvatarUrl());
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user,userLoginVO);
        userLoginVO.setJwtToken(jwt);
        return Result.success(userLoginVO);
    }

    @PostMapping("/update")
    public Result<UserLoginVO> update(@RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("用户更新{}",userUpdateDTO);
        User user = userService.updte(userUpdateDTO);

        //登录成功后，生成jwt
        Map<String, Object> claims=new HashMap<>();
        claims.put("userId",user.getId());
        claims.put("username",user.getUsername());
        claims.put("avatarUrl",user.getAvatarUrl());
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user,userLoginVO);
        userLoginVO.setJwtToken(jwt);

        return Result.success(userLoginVO);
    }

    @GetMapping("/public/{id}")
    public Result<UserLoginVO> userInfo(@PathVariable Long id) {
        log.info("渲染用户信息{}",id);
        User user = userService.userInfo(id);
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setUsername(user.getUsername());
        userLoginVO.setAvatarUrl(user.getAvatarUrl());
        return Result.success(userLoginVO);
    }

}
