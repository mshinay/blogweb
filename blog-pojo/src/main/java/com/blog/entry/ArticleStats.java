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
public class ArticleStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long articleId;

    private Long viewCount;

    private Long likeCount;

    private Long commentCount;

    private Long favoriteCount;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
