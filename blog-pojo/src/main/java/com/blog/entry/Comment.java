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

    private Long id;//文章id

    private Long articleId;//文章id

    private String content;//正文

    private Long userId;//作者id

    private Integer status;//状态 1:可视 0:不可视

    private LocalDateTime createTime;//创建时间


}
