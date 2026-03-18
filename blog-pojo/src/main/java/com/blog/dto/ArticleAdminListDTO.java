package com.blog.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleAdminListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long authorId;

    private Long categoryId;

    private Long tagId;

    private Integer status;

    private Integer allowComment;

    private Integer isTop;

    private String slug;

    private String keyword;

    private int page;

    private int pageSize;
}
