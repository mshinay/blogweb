package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AdminCommentListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long commentId;

    private Long articleId;

    private String articleTitle;

    private Integer articleStatus;

    private Long userId;

    private String userName;

    private Long replyUserId;

    private String replyUserName;

    private String content;

    private Integer status;

    private Long rootId;

    private Long parentId;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
