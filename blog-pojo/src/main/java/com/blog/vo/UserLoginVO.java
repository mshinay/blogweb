package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserLoginVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;//用户id

    private String username;//用户名

    private String email;//电子邮箱

    private Integer role;//角色 0:管理员 1:用户

    private String avatarUrl;//用户头像地址

    private LocalDateTime createTime;//创建时间

    private String jwtToken;

}
