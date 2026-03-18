package com.blog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleAdminListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Positive(message = "作者ID必须大于0")
    private Long authorId;

    @Positive(message = "分类ID必须大于0")
    private Long categoryId;

    @Positive(message = "标签ID必须大于0")
    private Long tagId;

    @Min(value = 0, message = "文章状态只能为0到2")
    @Max(value = 2, message = "文章状态只能为0到2")
    private Integer status;

    @Min(value = 0, message = "允许评论标记只能为0或1")
    @Max(value = 1, message = "允许评论标记只能为0或1")
    private Integer allowComment;

    @Min(value = 0, message = "置顶标记只能为0或1")
    @Max(value = 1, message = "置顶标记只能为0或1")
    private Integer isTop;

    @Size(max = 150, message = "slug长度不能超过150个字符")
    private String slug;

    @Size(max = 100, message = "关键字长度不能超过100个字符")
    private String keyword;

    @Min(value = 1, message = "页码必须大于等于1")
    private int page;

    @Min(value = 1, message = "每页条数必须大于等于1")
    private int pageSize;
}
