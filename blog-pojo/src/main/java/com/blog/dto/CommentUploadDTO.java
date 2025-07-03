package com.blog.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentUploadDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long articleId;
    private String content;
}
