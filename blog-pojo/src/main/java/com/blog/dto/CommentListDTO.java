package com.blog.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long articleId;

    private Long rootId;

    private int page;

    private int pageSize;
}
