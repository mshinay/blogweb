package com.blog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommentUserHistoryQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Positive(message = "文章ID必须大于0")
    private Long articleId;

    @Min(value = 1, message = "页码必须大于等于1")
    private int page;

    @Min(value = 1, message = "每页条数必须大于等于1")
    private int pageSize;
}
