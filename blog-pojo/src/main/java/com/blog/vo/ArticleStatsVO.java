package com.blog.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long viewCount;

    private Long likeCount;

    private Long commentCount;

    private Long favoriteCount;
}
