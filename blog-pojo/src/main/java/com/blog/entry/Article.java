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
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String slug;

    private String summary;

    private String coverUrl;

    private String content;

    private String contentPlain;

    private String contentType;

    private Long authorId;

    private Long categoryId;

    private Integer status;

    private Integer isTop;

    private Integer allowComment;

    private LocalDateTime publishTime;

    private Integer wordCount;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

}

