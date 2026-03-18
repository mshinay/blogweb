package com.blog.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    private String email;

    private String nickname;

    private String avatarUrl;

    private String bio;
}
