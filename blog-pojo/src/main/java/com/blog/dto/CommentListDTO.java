package com.blog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommentListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Positive(message = "文章ID必须大于0")
    private Long articleId;

    @Positive(message = "根评论ID必须大于0")
    private Long rootId;

    @Min(value = 1, message = "页码必须大于等于1")
    private int page;

    @Min(value = 1, message = "每页条数必须大于等于1")
    private int pageSize;
}
