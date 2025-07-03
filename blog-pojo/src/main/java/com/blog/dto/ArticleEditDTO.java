package com.blog.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleEditDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content;
}
