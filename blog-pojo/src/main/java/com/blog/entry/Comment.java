package com.blog.entry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long articleId;

    private Long userId;

    private Long parentId;

    private Long rootId;

    private Long replyUserId;

    private Long replyToCommentId;

    private String content;

    private Integer status;

    private Integer likeCount;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

}
