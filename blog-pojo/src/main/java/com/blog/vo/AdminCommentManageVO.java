package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AdminCommentManageVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long articleId;
    private String articleTitle;
    private List<CommentPreviewVO> comments;
}
