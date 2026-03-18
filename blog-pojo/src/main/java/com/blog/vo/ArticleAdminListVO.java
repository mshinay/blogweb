package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ArticleAdminListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String slug;

    private Long authorId;

    private String authorName;

    private Long categoryId;

    private String categoryName;

    private Integer status;

    private Integer isTop;

    private Integer allowComment;

    private LocalDateTime publishTime;

    private LocalDateTime updatedTime;

    private Long viewCount;

    private Long commentCount;
}
