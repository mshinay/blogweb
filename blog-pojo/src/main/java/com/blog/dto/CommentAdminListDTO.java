package com.blog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommentAdminListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Positive(message = "用户ID必须大于0")
    private Long userId;

    @Positive(message = "文章ID必须大于0")
    private Long articleId;

    @Min(value = 0, message = "父评论ID不能小于0")
    private Long parentId;

    @Min(value = 0, message = "根评论ID不能小于0")
    private Long rootId;

    @Positive(message = "被回复用户ID必须大于0")
    private Long replyUserId;

    @Min(value = 0, message = "评论状态只能为0到2")
    @Max(value = 2, message = "评论状态只能为0到2")
    private Integer status;

    @Size(max = 100, message = "关键字长度不能超过100个字符")
    private String keyword;

    @Min(value = 1, message = "页码必须大于等于1")
    private int page;

    @Min(value = 1, message = "每页条数必须大于等于1")
    private int pageSize;
}
