package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommentUploadDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "文章ID不能为空")
    @Positive(message = "文章ID必须大于0")
    private Long articleId;

    @Min(value = 0, message = "父评论ID不能小于0")
    private Long parentId;

    @Min(value = 0, message = "根评论ID不能小于0")
    private Long rootId;

    @Min(value = 0, message = "被回复用户ID不能小于0")
    private Long replyUserId;

    @Min(value = 0, message = "被回复评论ID不能小于0")
    private Long replyToCommentId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    private String content;
}
