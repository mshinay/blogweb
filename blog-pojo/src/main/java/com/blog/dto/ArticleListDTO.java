package com.blog.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long authorId;

    private String keyword;

    private int page;

    private int pageSize;

}
