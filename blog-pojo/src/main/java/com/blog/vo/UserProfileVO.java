package com.blog.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserProfileVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickname;

    private String avatarUrl;

    private String bio;
}
