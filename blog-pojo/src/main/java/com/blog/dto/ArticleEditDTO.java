package com.blog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArticleEditDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "文章ID不能为空")
    @Positive(message = "文章ID必须大于0")
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 150, message = "标题长度不能超过150个字符")
    private String title;

    @NotBlank(message = "slug不能为空")
    @Size(max = 150, message = "slug长度不能超过150个字符")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9-_/]*$", message = "slug格式不正确")
    private String slug;

    @Size(max = 300, message = "摘要长度不能超过300个字符")
    private String summary;

    @Size(max = 255, message = "封面地址长度不能超过255个字符")
    private String coverUrl;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    @Pattern(regexp = "^(markdown|html)?$", message = "内容类型仅支持markdown或html")
    private String contentType;

    @Positive(message = "分类ID必须大于0")
    private Long categoryId;

    @Size(max = 10, message = "标签数量不能超过10个")
    private List<@NotNull(message = "标签ID不能为空") @Positive(message = "标签ID必须大于0") Long> tagIds;

    @Min(value = 0, message = "允许评论标记只能为0或1")
    @Max(value = 1, message = "允许评论标记只能为0或1")
    private Integer allowComment;

    @Min(value = 0, message = "文章状态只能为0或1")
    @Max(value = 1, message = "文章状态只能为0或1")
    private Integer status;
}
