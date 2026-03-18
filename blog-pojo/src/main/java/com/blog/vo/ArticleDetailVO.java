package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String slug;

    private String summary;

    private String coverUrl;

    private String content;

    private String contentType;

    private UserProfileVO author;

    private CategoryVO category;

    private List<TagVO> tags;

    private ArticleStatsVO stats;

    private Integer allowComment;

    private LocalDateTime publishTime;

    private LocalDateTime updatedTime;

    private Integer wordCount;

    private List<CommentTreeVO> comments;
}
