package com.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Positive(message = "用户ID必须大于0")
    private Long id;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Size(max = 255, message = "头像地址长度不能超过255个字符")
    private String avatarUrl;

    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;
}
