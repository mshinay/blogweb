package com.boot.blogserver.controller;

import com.blog.dto.UserLoginDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.dto.UserUpdateDTO;
import com.blog.entry.User;
import com.blog.exception.BusinessException;
import com.blog.properties.JwtProperties;
import com.blog.utils.JwtUtil;
import com.blog.vo.UserLoginVO;
import com.blog.vo.UserProfileVO;
import com.boot.blogserver.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.blog.result.Result;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
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
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录 username={}", userLoginDTO.getUsername());
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
        log.info("用户登录 返回结果={}", userLoginVO);
        return Result.success(userLoginVO);
    }

    /**
     * 用户注册
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("/register")
    public Result<UserLoginVO> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册 username={}, email={}", userRegisterDTO.getUsername(), userRegisterDTO.getEmail());
        User user = userService.register(userRegisterDTO);
        return Result.success(buildLoginVO(user));
    }

    @PostMapping("/update")
    public Result<UserLoginVO> update(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        if (userUpdateDTO.getId() == null) {
            throw new BusinessException(Result.VALIDATION_ERROR, 400, "用户ID不能为空");
        }
        log.info("用户更新 id={}", userUpdateDTO.getId());
        User user = userService.updte(userUpdateDTO);
        return Result.success(buildLoginVO(user));
    }

    @PutMapping("/{id}")
    public Result<UserLoginVO> updateById(@Positive(message = "用户ID必须大于0") @PathVariable Long id,
                                          @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        if (userUpdateDTO.getId() != null && !id.equals(userUpdateDTO.getId())) {
            throw new BusinessException("路径用户ID与请求体用户ID不一致");
        }
        userUpdateDTO.setId(id);
        log.info("用户按标准路径更新 id={}", id);
        User user = userService.updte(userUpdateDTO);
        return Result.success(buildLoginVO(user));
    }

    @GetMapping({"/public/{id}", "/{id}"})
    public Result<UserProfileVO> userInfo(@Positive(message = "用户ID必须大于0") @PathVariable Long id) {
        log.info("渲染用户信息{}",id);
        User user = userService.userInfo(id);
        UserProfileVO userProfileVO = new UserProfileVO();
        BeanUtils.copyProperties(user, userProfileVO);
        return Result.success(userProfileVO);
    }

    private UserLoginVO buildLoginVO(User user) {
        Map<String, Object> claims=new HashMap<>();
        claims.put("userId",user.getId());
        claims.put("username",user.getUsername());
        claims.put("avatarUrl",user.getAvatarUrl());
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user,userLoginVO);
        userLoginVO.setJwtToken(jwt);
        return userLoginVO;
    }

}
