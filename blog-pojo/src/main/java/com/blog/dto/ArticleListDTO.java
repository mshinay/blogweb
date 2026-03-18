package com.blog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Positive(message = "作者ID必须大于0")
    private Long authorId;

    @Positive(message = "分类ID必须大于0")
    private Long categoryId;

    @Positive(message = "标签ID必须大于0")
    private Long tagId;

    @Size(max = 150, message = "slug长度不能超过150个字符")
    private String slug;

    @Size(max = 100, message = "关键字长度不能超过100个字符")
    private String keyword;

    @Min(value = 1, message = "页码必须大于等于1")
    private int page;

    @Min(value = 1, message = "每页条数必须大于等于1")
    private int pageSize;
}
