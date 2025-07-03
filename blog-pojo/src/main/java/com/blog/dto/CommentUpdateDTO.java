package com.blog.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String content;
}
