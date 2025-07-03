package com.blog.dto;

import lombok.Data;

@Data
public class CommentListDTO {
    private static final long serialVersionUID = 1L;

    private Long userId;//作者id

    private Long articleId;//文章id

    private String keyword;

    private int page;

    private int pageSize;
}
