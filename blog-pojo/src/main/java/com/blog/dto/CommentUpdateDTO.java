package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommentUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "评论ID不能为空")
    @Positive(message = "评论ID必须大于0")
    private Long id;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    private String content;
}
