package com.blog.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArticleEditDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String slug;

    private String summary;

    private String coverUrl;

    private String content;

    private String contentType;

    private Long categoryId;

    private List<Long> tagIds;

    private Integer allowComment;

    private Integer status;
}
