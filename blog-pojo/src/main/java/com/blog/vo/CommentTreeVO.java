package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CommentTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private CommentPreviewVO comment;

    private List<CommentPreviewVO> children;
}
