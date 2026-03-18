package com.blog.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String email;

    private String nickname;

    private String avatarUrl;

    private String bio;
}
