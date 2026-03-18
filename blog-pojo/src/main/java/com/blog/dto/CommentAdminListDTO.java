package com.blog.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentAdminListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long articleId;

    private Long parentId;

    private Long rootId;

    private Long replyUserId;

    private Integer status;

    private String keyword;

    private int page;

    private int pageSize;
}
