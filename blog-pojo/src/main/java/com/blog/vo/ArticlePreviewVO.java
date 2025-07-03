package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ArticlePreviewVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;//文章id

    private String title;//标题

    private String summary;//

    private String authorName;

    private Integer status;//状态 0:可视 1:不可视

    private LocalDateTime createTime;//创建时间


}
